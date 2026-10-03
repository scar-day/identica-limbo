package dev.scarday.identicalimbo.common;

import dev.scarday.identicalimbo.api.IdenticaLimboAPI;
import dev.scarday.identicalimbo.api.VirtualServerDefinition;
import dev.scarday.identicalimbo.api.VirtualServerStatus;
import dev.scarday.identicalimbo.api.provider.LimboProvider;
import dev.scarday.identicalimbo.api.provider.LimboProviderRegistry;
import dev.scarday.identicalimbo.api.provider.LimboServerContext;
import dev.scarday.identicalimbo.api.provider.ManagedLimboServer;
import dev.scarday.identicalimbo.api.provider.ServerRegistrar;
import dev.scarday.identicalimbo.api.provider.SimpleLimboProviderRegistry;
import dev.scarday.identicalimbo.common.config.LimboCommandMessages;
import dev.scarday.identicalimbo.common.config.LimboConfigurationLoader;
import dev.scarday.identicalimbo.common.config.VirtualServerDocument;
import dev.scarday.identicalimbo.common.logging.LimboLogger;
import dev.scarday.identicalimbo.common.provider.picolimbo.PicoLimboProvider;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import me.whereareiam.configura.Configura;
import me.whereareiam.identica.model.CommandDefinition;

public abstract class AbstractLimboService implements LimboService, AutoCloseable {
    protected final Path dataDirectory;
    protected final Configura configura;
    protected final LimboLogger logger;
    protected final LimboProviderRegistry providerRegistry = new SimpleLimboProviderRegistry();
    protected final Map<String, ManagedLimboServer> activeServers = new ConcurrentHashMap<>();
    protected final Map<String, CommandDefinition> commands = new LinkedHashMap<>();
    protected final Map<String, CommandDefinition> commandDefinitions = Collections.unmodifiableMap(commands);
    protected final LimboCommandMessages messages = new LimboCommandMessages();
    protected ServerRegistrar registrar;

    public AbstractLimboService(Path dataDirectory, Configura configura, LimboLogger logger) {
        this.dataDirectory = dataDirectory;
        this.configura = configura;
        this.logger = logger;
        this.providerRegistry.register(new PicoLimboProvider(logger));
    }

    public void start() {
        IdenticaLimboAPI.set(this);
        reload();
    }

    protected abstract ServerRegistrar createServerRegistrar();

    @Override
    public synchronized void reload() {
        LimboConfiguration configuration;
        try {
            configuration = new LimboConfigurationLoader(configura).load(dataDirectory);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load Identica Limbo configuration", exception);
        }

        try {
            closeServers();
            registrar = createServerRegistrar();
            for (VirtualServerDefinition definition : configuration.servers()) {
                LimboProvider provider = providerRegistry.find(definition.provider())
                        .orElseThrow(() -> new IllegalStateException("Unsupported limbo provider: " + definition.provider()));
                VirtualServerDocument document = configuration.serverDocument(definition.name());
                LimboServerContext context = createContext(document, registrar);
                ManagedLimboServer server = provider.createServer(definition, context);
                activeServers.put(definition.name().toLowerCase(Locale.ROOT), server);
            }
            updateCommandConfiguration(configuration);
        } catch (Exception exception) {
            try {
                closeServers();
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw new IllegalStateException("Unable to reload Identica Limbo servers", exception);
        }
    }

    @Override
    public List<VirtualServerDefinition> servers() {
        return activeServers.values().stream()
                .map(ManagedLimboServer::definition)
                .toList();
    }

    @Override
    public Optional<VirtualServerDefinition> find(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        ManagedLimboServer server = activeServers.get(name.trim().toLowerCase(Locale.ROOT));
        return server != null ? Optional.of(server.definition()) : Optional.empty();
    }

    @Override
    public Optional<VirtualServerStatus> status(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        ManagedLimboServer server = activeServers.get(name.trim().toLowerCase(Locale.ROOT));
        if (server == null) {
            return Optional.empty();
        }
        List<String> playerNames = getConnectedPlayers(server.definition().name());
        int online = playerNames.size();
        return Optional.of(server.buildStatus(online, playerNames));
    }

    @Override
    public boolean isLimbo(String serverName) {
        if (serverName == null || serverName.isBlank()) {
            return false;
        }
        return activeServers.containsKey(serverName.trim().toLowerCase(Locale.ROOT));
    }

    @Override
    public int getOnlineCount(String limboName) {
        return getConnectedPlayers(limboName).size();
    }

    @Override
    public int getTotalOnlineCount() {
        return activeServers.keySet().stream()
                .mapToInt(this::getOnlineCount)
                .sum();
    }

    @Override
    public CompletableFuture<Boolean> transfer(String playerName, String limboName) {
        if (playerName == null || playerName.isBlank() || limboName == null || limboName.isBlank()) {
            return CompletableFuture.completedFuture(false);
        }
        UUID playerUuid = findPlayerUuid(playerName.trim());
        if (playerUuid == null) {
            return CompletableFuture.completedFuture(false);
        }
        return transfer(playerUuid, limboName);
    }

    @Override
    public CompletableFuture<List<UUID>> transferAll(String limboName) {
        if (limboName == null || limboName.isBlank() || !isLimbo(limboName)) {
            return CompletableFuture.completedFuture(List.of());
        }
        List<UUID> playerUuids = getAllPlayerUuids();
        List<CompletableFuture<UUID>> futures = playerUuids.stream()
                .map(uuid -> transfer(uuid, limboName)
                        .thenApply(success -> Boolean.TRUE.equals(success) ? uuid : null))
                .toList();
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .toList());
    }

    @Override
    public CompletableFuture<List<UUID>> transferAll(String sourceServerName, String limboName) {
        if (sourceServerName == null || sourceServerName.isBlank() || limboName == null || limboName.isBlank() || !isLimbo(limboName)) {
            return CompletableFuture.completedFuture(List.of());
        }
        List<UUID> playerUuids = getPlayerUuidsConnectedTo(sourceServerName.trim());
        List<CompletableFuture<UUID>> futures = playerUuids.stream()
                .map(uuid -> transfer(uuid, limboName)
                        .thenApply(success -> Boolean.TRUE.equals(success) ? uuid : null))
                .toList();
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .toList());
    }

    @Override
    public LimboProviderRegistry providers() {
        return providerRegistry;
    }

    public synchronized Map<String, CommandDefinition> commands() {
        return commandDefinitions;
    }

    public LimboCommandMessages messages() {
        return messages;
    }

    private void updateCommandConfiguration(LimboConfiguration configuration) {
        commands.clear();
        commands.putAll(configuration.commands());
        messages.copyFrom(configuration.messages());
    }

    protected LimboServerContext createContext(VirtualServerDocument document, ServerRegistrar serverRegistrar) {
        return new LimboServerContext() {
            @Override
            public ServerRegistrar registrar() {
                return serverRegistrar;
            }

            @Override
            public Path dataDirectory() {
                return dataDirectory;
            }

            @Override
            public Object document() {
                return document;
            }

            @Override
            public void logInfo(String message, Object... args) {
                logger.info(message, args);
            }

            @Override
            public void logWarn(String message, Object... args) {
                logger.warn(message, args);
            }

            @Override
            public void logError(String message, Throwable throwable) {
                logger.error(message, throwable);
            }
        };
    }

    protected synchronized void closeServers() {
        RuntimeException cleanupFailure = null;
        List<ManagedLimboServer> servers = new ArrayList<>(activeServers.values());
        activeServers.clear();
        ServerRegistrar serverRegistrar = registrar;
        registrar = null;

        for (ManagedLimboServer server : servers) {
            try {
                server.close();
            } catch (Exception exception) {
                if (cleanupFailure == null) {
                    cleanupFailure = new RuntimeException("Failure during server cleanup", exception);
                } else {
                    cleanupFailure.addSuppressed(exception);
                }
            }
        }
        if (serverRegistrar != null) {
            try {
                serverRegistrar.unregisterAll();
            } catch (RuntimeException exception) {
                if (cleanupFailure == null) {
                    cleanupFailure = exception;
                } else {
                    cleanupFailure.addSuppressed(exception);
                }
            }
        }
        if (cleanupFailure != null) {
            throw cleanupFailure;
        }
    }

    @Override
    public synchronized void close() {
        IdenticaLimboAPI.set(null);
        closeServers();
    }

    public void registerActiveServerForTesting(String name, ManagedLimboServer server) {
        if (name != null && server != null) {
            activeServers.put(name.trim().toLowerCase(Locale.ROOT), server);
        }
    }

    protected abstract UUID findPlayerUuid(String playerName);

    protected abstract List<UUID> getAllPlayerUuids();

    protected abstract List<UUID> getPlayerUuidsConnectedTo(String serverName);
}

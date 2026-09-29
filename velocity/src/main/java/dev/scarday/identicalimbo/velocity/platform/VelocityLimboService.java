package dev.scarday.identicalimbo.velocity.platform;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.scarday.identicalimbo.api.IdenticaLimboAPI;
import dev.scarday.identicalimbo.api.VirtualServerDefinition;
import dev.scarday.identicalimbo.api.VirtualServerStatus;
import dev.scarday.identicalimbo.api.provider.LimboProvider;
import dev.scarday.identicalimbo.api.provider.LimboProviderRegistry;
import dev.scarday.identicalimbo.api.provider.LimboServerContext;
import dev.scarday.identicalimbo.api.provider.ManagedLimboServer;
import dev.scarday.identicalimbo.api.provider.ServerRegistrar;
import dev.scarday.identicalimbo.api.provider.SimpleLimboProviderRegistry;
import dev.scarday.identicalimbo.common.LimboConfiguration;
import dev.scarday.identicalimbo.common.LimboService;
import dev.scarday.identicalimbo.common.config.LimboCommandMessages;
import dev.scarday.identicalimbo.common.config.LimboConfigurationLoader;
import dev.scarday.identicalimbo.common.config.VirtualServerDocument;
import dev.scarday.identicalimbo.velocity.provider.picolimbo.PicoLimboProvider;
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
import org.slf4j.Logger;

public final class VelocityLimboService implements LimboService {
    private final ProxyServer proxyServer;
    private final Path dataDirectory;
    private final Configura configura;
    private final Logger logger;
    private final LimboProviderRegistry providerRegistry = new SimpleLimboProviderRegistry();
    private final Map<String, ManagedLimboServer> activeServers = new ConcurrentHashMap<>();
    private final Map<String, CommandDefinition> commands = new LinkedHashMap<>();
    private final Map<String, CommandDefinition> commandDefinitions = Collections.unmodifiableMap(commands);
    private final LimboCommandMessages messages = new LimboCommandMessages();
    private VelocityServerRegistrar registrar;

    public VelocityLimboService(ProxyServer proxyServer, Path dataDirectory, Configura configura, Logger logger) {
        this.proxyServer = proxyServer;
        this.dataDirectory = dataDirectory;
        this.configura = configura;
        this.logger = logger;
        this.providerRegistry.register(new PicoLimboProvider(logger));
    }

    public void start() {
        IdenticaLimboAPI.set(this);
        reload();
    }

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
            registrar = new VelocityServerRegistrar(proxyServer);
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
    public List<String> getConnectedPlayers(String limboName) {
        if (limboName == null || limboName.isBlank()) {
            return List.of();
        }
        return proxyServer.getServer(limboName.trim())
                .map(server -> server.getPlayersConnected().stream().map(Player::getUsername).sorted().toList())
                .orElse(List.of());
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
    public CompletableFuture<Boolean> transfer(UUID playerUuid, String limboName) {
        if (playerUuid == null || limboName == null || limboName.isBlank()) {
            return CompletableFuture.completedFuture(false);
        }
        Player player = proxyServer.getPlayer(playerUuid).orElse(null);
        if (player == null) {
            return CompletableFuture.completedFuture(false);
        }
        RegisteredServer target = proxyServer.getServer(limboName.trim()).orElse(null);
        if (target == null || !isLimbo(limboName)) {
            return CompletableFuture.completedFuture(false);
        }
        return player.createConnectionRequest(target)
                .connect()
                .thenApply(ConnectionRequestBuilder.Result::isSuccessful);
    }

    @Override
    public CompletableFuture<Boolean> transfer(String playerName, String limboName) {
        if (playerName == null || playerName.isBlank() || limboName == null || limboName.isBlank()) {
            return CompletableFuture.completedFuture(false);
        }
        Player player = proxyServer.getPlayer(playerName.trim()).orElse(null);
        if (player == null) {
            return CompletableFuture.completedFuture(false);
        }
        return transfer(player.getUniqueId(), limboName);
    }

    @Override
    public CompletableFuture<List<UUID>> transferAll(String limboName) {
        if (limboName == null || limboName.isBlank() || !isLimbo(limboName)) {
            return CompletableFuture.completedFuture(List.of());
        }
        List<CompletableFuture<UUID>> futures = proxyServer.getAllPlayers().stream()
                .map(player -> transfer(player.getUniqueId(), limboName)
                        .thenApply(success -> Boolean.TRUE.equals(success) ? player.getUniqueId() : null))
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
        RegisteredServer source = proxyServer.getServer(sourceServerName.trim()).orElse(null);
        if (source == null) {
            return CompletableFuture.completedFuture(List.of());
        }
        List<CompletableFuture<UUID>> futures = source.getPlayersConnected().stream()
                .map(player -> transfer(player.getUniqueId(), limboName)
                        .thenApply(success -> Boolean.TRUE.equals(success) ? player.getUniqueId() : null))
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

    private LimboServerContext createContext(VirtualServerDocument document, ServerRegistrar serverRegistrar) {
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

    private synchronized void closeServers() {
        RuntimeException cleanupFailure = null;
        List<ManagedLimboServer> servers = new ArrayList<>(activeServers.values());
        activeServers.clear();
        VelocityServerRegistrar serverRegistrar = registrar;
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

    public synchronized void close() {
        IdenticaLimboAPI.set(null);
        closeServers();
    }
}

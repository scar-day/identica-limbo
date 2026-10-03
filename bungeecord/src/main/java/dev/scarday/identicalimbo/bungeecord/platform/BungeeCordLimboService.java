package dev.scarday.identicalimbo.bungeecord.platform;

import dev.scarday.identicalimbo.api.provider.ServerRegistrar;
import dev.scarday.identicalimbo.common.AbstractLimboService;
import dev.scarday.identicalimbo.provider.picolimbo.PicoLimboProvider;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import me.whereareiam.configura.Configura;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class BungeeCordLimboService extends AbstractLimboService {
    private final ProxyServer proxyServer;

    public BungeeCordLimboService(ProxyServer proxyServer, Path dataDirectory, Configura configura, Logger logger) {
        super(dataDirectory, configura, new BungeeCordLimboLogger(logger));
        this.proxyServer = proxyServer;
        providerRegistry.register(new PicoLimboProvider(this.logger));
    }

    @Override
    protected ServerRegistrar createServerRegistrar() {
        return new BungeeCordServerRegistrar(proxyServer);
    }

    @Override
    public List<String> getConnectedPlayers(String limboName) {
        if (limboName == null || limboName.isBlank()) {
            return List.of();
        }
        ServerInfo server = proxyServer.getServerInfo(limboName.trim());
        if (server == null) {
            return List.of();
        }
        return server.getPlayers().stream().map(ProxiedPlayer::getName).sorted().toList();
    }

    @Override
    public CompletableFuture<Boolean> transfer(UUID playerUuid, String limboName) {
        if (playerUuid == null || limboName == null || limboName.isBlank()) {
            return CompletableFuture.completedFuture(false);
        }
        ProxiedPlayer player = proxyServer.getPlayer(playerUuid);
        if (player == null) {
            return CompletableFuture.completedFuture(false);
        }
        ServerInfo target = proxyServer.getServerInfo(limboName.trim());
        if (target == null || !isLimbo(limboName)) {
            return CompletableFuture.completedFuture(false);
        }
        if (player.getServer() != null && player.getServer().getInfo().equals(target)) {
            return CompletableFuture.completedFuture(true);
        }
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            player.connect(target, (result, error) -> {
                if (error != null) {
                    future.complete(false);
                } else {
                    future.complete(Boolean.TRUE.equals(result));
                }
            });
        } catch (Exception exception) {
            future.complete(false);
        }
        return future;
    }

    @Override
    protected UUID findPlayerUuid(String playerName) {
        ProxiedPlayer player = proxyServer.getPlayer(playerName);
        return player != null ? player.getUniqueId() : null;
    }

    @Override
    protected List<UUID> getAllPlayerUuids() {
        return proxyServer.getPlayers().stream().map(ProxiedPlayer::getUniqueId).toList();
    }

    @Override
    protected List<UUID> getPlayerUuidsConnectedTo(String serverName) {
        ServerInfo server = proxyServer.getServerInfo(serverName);
        if (server == null) {
            return List.of();
        }
        return server.getPlayers().stream().map(ProxiedPlayer::getUniqueId).toList();
    }
}

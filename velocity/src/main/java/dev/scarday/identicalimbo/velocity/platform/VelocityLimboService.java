package dev.scarday.identicalimbo.velocity.platform;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.scarday.identicalimbo.api.provider.ServerRegistrar;
import dev.scarday.identicalimbo.common.AbstractLimboService;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.whereareiam.configura.Configura;
import org.slf4j.Logger;

public final class VelocityLimboService extends AbstractLimboService {
    private final ProxyServer proxyServer;

    public VelocityLimboService(ProxyServer proxyServer, Path dataDirectory, Configura configura, Logger logger) {
        super(dataDirectory, configura, new VelocityLimboLogger(logger));
        this.proxyServer = proxyServer;
    }

    @Override
    protected ServerRegistrar createServerRegistrar() {
        return new VelocityServerRegistrar(proxyServer);
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
    protected UUID findPlayerUuid(String playerName) {
        return proxyServer.getPlayer(playerName).map(Player::getUniqueId).orElse(null);
    }

    @Override
    protected List<UUID> getAllPlayerUuids() {
        return proxyServer.getAllPlayers().stream().map(Player::getUniqueId).toList();
    }

    @Override
    protected List<UUID> getPlayerUuidsConnectedTo(String serverName) {
        return proxyServer.getServer(serverName)
                .map(server -> server.getPlayersConnected().stream().map(Player::getUniqueId).toList())
                .orElse(List.of());
    }
}

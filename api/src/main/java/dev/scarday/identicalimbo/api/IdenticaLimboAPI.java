package dev.scarday.identicalimbo.api;

import dev.scarday.identicalimbo.api.provider.LimboProviderRegistry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IdenticaLimboAPI {
    static IdenticaLimboAPI get() {
        return IdenticaLimboAPIHolder.get();
    }

    static void set(IdenticaLimboAPI api) {
        IdenticaLimboAPIHolder.set(api);
    }

    List<VirtualServerDefinition> servers();

    Optional<VirtualServerDefinition> find(String name);

    Optional<VirtualServerStatus> status(String name);

    boolean isLimbo(String serverName);

    List<String> getConnectedPlayers(String limboName);

    int getOnlineCount(String limboName);

    int getTotalOnlineCount();

    CompletableFuture<Boolean> transfer(UUID playerUuid, String limboName);

    CompletableFuture<Boolean> transfer(String playerName, String limboName);

    CompletableFuture<List<UUID>> transferAll(String limboName);

    CompletableFuture<List<UUID>> transferAll(String sourceServerName, String limboName);

    LimboProviderRegistry providers();

    void reload();
}

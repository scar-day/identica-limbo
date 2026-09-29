package dev.scarday.identicalimbo.api;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record VirtualServerStatus(
        VirtualServerDefinition definition,
        boolean running,
        int onlinePlayers,
        List<String> playerNames,
        long memoryBytes,
        long maxMemoryBytes,
        Instant startedAt,
        Long pid,
        String dimension,
        String gameMode,
        String schematic
) {
    public VirtualServerStatus {
        Objects.requireNonNull(definition, "definition");
        playerNames = playerNames == null ? List.of() : List.copyOf(playerNames);
    }

    public String name() {
        return definition.name();
    }

    public LimboProviderType provider() {
        return definition.provider();
    }

    public String host() {
        return definition.host();
    }

    public int port() {
        return definition.port();
    }
}

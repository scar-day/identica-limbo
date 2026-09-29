package dev.scarday.identicalimbo.api.provider;

import dev.scarday.identicalimbo.api.VirtualServerDefinition;
import dev.scarday.identicalimbo.api.VirtualServerStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ManagedLimboServer extends AutoCloseable {
    VirtualServerDefinition definition();

    boolean isRunning();

    Optional<Long> pid();

    long memoryBytes();

    Instant startedAt();

    String dimension();

    String gameMode();

    String schematic();

    @Override
    void close();

    default VirtualServerStatus buildStatus(int onlinePlayers, List<String> playerNames) {
        return new VirtualServerStatus(
                definition(),
                isRunning(),
                onlinePlayers,
                playerNames,
                memoryBytes(),
                -1L,
                startedAt(),
                pid().orElse(null),
                dimension(),
                gameMode(),
                schematic()
        );
    }
}

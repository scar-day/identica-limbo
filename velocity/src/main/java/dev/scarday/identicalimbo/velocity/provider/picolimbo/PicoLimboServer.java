package dev.scarday.identicalimbo.velocity.provider.picolimbo;

import dev.scarday.identicalimbo.api.VirtualServerDefinition;
import dev.scarday.identicalimbo.common.PicoLimboSettings;
import java.time.Instant;

public class PicoLimboServer extends dev.scarday.identicalimbo.common.provider.picolimbo.PicoLimboServer {
    public PicoLimboServer(VirtualServerDefinition definition, PicoLimboSettings settings, Process process, Instant startedAt) {
        super(definition, settings, process, startedAt);
    }
}

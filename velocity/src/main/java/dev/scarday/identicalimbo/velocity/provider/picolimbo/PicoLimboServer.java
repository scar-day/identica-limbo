package dev.scarday.identicalimbo.velocity.provider.picolimbo;

import dev.scarday.identicalimbo.api.VirtualServerDefinition;
import dev.scarday.identicalimbo.provider.picolimbo.PicoLimboSettings;
import java.time.Instant;

@Deprecated
public class PicoLimboServer extends dev.scarday.identicalimbo.provider.picolimbo.PicoLimboServer {
    public PicoLimboServer(VirtualServerDefinition definition, PicoLimboSettings settings, Process process, Instant startedAt) {
        super(definition, settings, process, startedAt);
    }
}

package dev.scarday.identicalimbo.api.provider;

import dev.scarday.identicalimbo.api.LimboProviderType;
import dev.scarday.identicalimbo.api.VirtualServerDefinition;

public interface LimboProvider {
    LimboProviderType type();

    ManagedLimboServer createServer(VirtualServerDefinition definition, LimboServerContext context) throws Exception;
}

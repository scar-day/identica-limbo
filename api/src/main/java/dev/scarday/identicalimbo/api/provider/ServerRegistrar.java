package dev.scarday.identicalimbo.api.provider;

import dev.scarday.identicalimbo.api.VirtualServerDefinition;

public interface ServerRegistrar {
    void register(VirtualServerDefinition definition);

    void unregister(String serverName);

    void unregisterAll();
}

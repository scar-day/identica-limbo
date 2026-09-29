package dev.scarday.identicalimbo.api;

import java.util.Objects;

public record VirtualServerDefinition(
        String name,
        LimboProviderType provider,
        String host,
        int port
) {
    public VirtualServerDefinition {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(host, "host");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (host.isBlank()) {
            throw new IllegalArgumentException("host must not be blank");
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port must be between 0 and 65535");
        }
    }
}

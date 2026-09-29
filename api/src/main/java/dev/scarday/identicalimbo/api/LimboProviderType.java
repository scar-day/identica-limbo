package dev.scarday.identicalimbo.api;

import java.util.Locale;
import java.util.Objects;

public record LimboProviderType(String name) {
    public static final LimboProviderType PICOLIMBO = new LimboProviderType("PICOLIMBO");

    public LimboProviderType {
        Objects.requireNonNull(name, "name");
        name = name.trim().toUpperCase(Locale.ROOT);
        if (name.isBlank()) {
            throw new IllegalArgumentException("LimboProviderType name must not be blank");
        }
    }

    public static LimboProviderType of(String name) {
        return new LimboProviderType(name);
    }

    public static LimboProviderType valueOf(String name) {
        return of(name);
    }

    @Override
    public String toString() {
        return name;
    }
}

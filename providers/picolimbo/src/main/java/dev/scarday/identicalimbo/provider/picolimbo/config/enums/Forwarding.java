package dev.scarday.identicalimbo.provider.picolimbo.config.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum Forwarding {
    NONE("NONE"),
    MODERN("MODERN"),
    BUNGEE_GUARD("BUNGEE_GUARD");

    private final String id;

    Forwarding(String id) {
        this.id = id;
    }

    @JsonValue
    public String getId() {
        return id;
    }

    @JsonCreator
    public static Forwarding fromString(String value) {
        if (value == null || value.isBlank()) {
            return NONE;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (Forwarding forwarding : values()) {
            if (forwarding.name().equals(normalized) || forwarding.id.equalsIgnoreCase(value.trim())) {
                return forwarding;
            }
        }
        throw new IllegalArgumentException(
                "picolimbo.forwarding.method must be NONE, MODERN or BUNGEE_GUARD, but got: " + value
        );
    }
}

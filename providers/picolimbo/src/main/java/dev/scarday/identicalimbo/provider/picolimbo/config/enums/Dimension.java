package dev.scarday.identicalimbo.provider.picolimbo.config.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum Dimension {
    OVERWORLD("overworld"),
    NETHER("nether"),
    END("end");

    private final String id;

    Dimension(String id) {
        this.id = id;
    }

    @JsonValue
    public String getId() {
        return id;
    }

    @JsonCreator
    public static Dimension fromString(String value) {
        if (value == null || value.isBlank()) {
            return OVERWORLD;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (Dimension dim : values()) {
            if (dim.id.equals(normalized) || dim.name().equalsIgnoreCase(normalized)) {
                return dim;
            }
        }
        throw new IllegalArgumentException(
                "picolimbo.world.dimension must be overworld, nether or end, but got: " + value
        );
    }
}

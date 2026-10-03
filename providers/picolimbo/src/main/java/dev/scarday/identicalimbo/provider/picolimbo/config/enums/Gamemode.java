package dev.scarday.identicalimbo.provider.picolimbo.config.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum Gamemode {
    SURVIVAL("survival"),
    CREATIVE("creative"),
    ADVENTURE("adventure"),
    SPECTATOR("spectator");

    private final String id;

    Gamemode(String id) {
        this.id = id;
    }

    @JsonValue
    public String getId() {
        return id;
    }

    @JsonCreator
    public static Gamemode fromString(String value) {
        if (value == null || value.isBlank()) {
            return SPECTATOR;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (Gamemode mode : values()) {
            if (mode.id.equals(normalized) || mode.name().equalsIgnoreCase(normalized)) {
                return mode;
            }
        }
        throw new IllegalArgumentException(
                "picolimbo.world.gameMode must be survival, creative, adventure or spectator, but got: " + value
        );
    }
}

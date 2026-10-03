package dev.scarday.identicalimbo.provider.picolimbo.config;

import java.util.Locale;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class PicoLimboDocument {
    private Paths paths = new Paths();
    private Lifecycle lifecycle = new Lifecycle();
    private Release release = new Release();
    private World world = new World();
    private Forwarding forwarding = new Forwarding();
    private boolean logging = true;

    public void validate() {
        if (paths == null) paths = new Paths();
        if (lifecycle == null) lifecycle = new Lifecycle();
        if (release == null) release = new Release();
        if (world == null) world = new World();
        if (forwarding == null) forwarding = new Forwarding();
        world.validate();
        if (!Set.of("NONE", "MODERN", "BUNGEE_GUARD")
                .contains(forwarding.method.toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("picolimbo.forwarding.method must be NONE, MODERN or BUNGEE_GUARD");
        }
        if (release.version == null || release.version.isBlank()) {
            throw new IllegalArgumentException("picolimbo.release.version must not be blank");
        }
    }

    @Getter
    @Setter
    public static final class Paths {
        private String download = "";
        private String executable = "";
        private String workingDirectory = "";
    }

    @Getter
    @Setter
    public static final class Lifecycle {
        private boolean autoStart = true;
        private boolean autoDownload = true;
    }

    @Getter
    @Setter
    public static final class Release {
        private String version = "latest";
    }

    @Getter
    @Setter
    public static final class World {
        private String schematic = "";
        private String welcomeMessage = "";
        private String actionBar = "";
        private String gameMode = "spectator";
        private String dimension = "overworld";
        private Spawn spawn = new Spawn();
        private int viewDistance = 2;
        private boolean lockTime = false;

        private void validate() {
            if (spawn == null) spawn = new Spawn();
            spawn.parsePosition();
            spawn.parseRotation();
            if (viewDistance < 1) {
                throw new IllegalArgumentException("picolimbo.world.viewDistance must be at least 1");
            }
            if (gameMode == null || !Set.of("survival", "creative", "adventure", "spectator")
                    .contains(gameMode.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException(
                        "picolimbo.world.gameMode must be survival, creative, adventure or spectator"
                );
            }
            if (dimension == null || !Set.of("overworld", "nether", "end")
                    .contains(dimension.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("picolimbo.world.dimension must be overworld, nether or end");
            }
        }
    }

    @Getter
    @Setter
    public static final class Spawn {
        @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = SemicolonCoordinateDeserializer.class)
        private String position = "20.5;17.0;22.5";

        @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = SemicolonCoordinateDeserializer.class)
        private String rotation = "-90.0;0.0";

        public double[] parsePosition() {
            return parse(position, 3, "position");
        }

        public double[] parseRotation() {
            return parse(rotation, 2, "rotation");
        }

        private static double[] parse(String raw, int expectedLength, String fieldName) {
            if (raw == null || raw.isBlank()) {
                throw new IllegalArgumentException("picolimbo.world.spawn." + fieldName + " must not be blank");
            }
            String[] parts = raw.split(";");
            if (parts.length != expectedLength) {
                throw new IllegalArgumentException(
                        "picolimbo.world.spawn." + fieldName + " must contain " + expectedLength + " numbers separated by ';'"
                );
            }
            double[] result = new double[expectedLength];
            for (int i = 0; i < expectedLength; i++) {
                try {
                    result[i] = Double.parseDouble(parts[i].trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Invalid number in picolimbo.world.spawn." + fieldName + ": " + parts[i].trim()
                    );
                }
            }
            return result;
        }
    }

    @Getter
    @Setter
    public static final class Forwarding {
        private String method = "NONE";
        private String secret = "";
    }
}

package dev.scarday.identicalimbo.common;

import java.nio.file.Path;
import java.util.Objects;

public record PicoLimboSettings(
        String downloadPath,
        String picoPath,
        Path workingDirectory,
        boolean autoStart,
        boolean autoDownload,
        String version,
        String schematic,
        String welcomeMessage,
        String actionBar,
        String defaultGameMode,
        String dimension,
        double[] spawnPosition,
        double[] spawnRotation,
        int viewDistance,
        boolean lockTime,
        String forwardingMethod,
        String forwardingSecret,
        boolean logging
) {
    public PicoLimboSettings {
        downloadPath = Objects.requireNonNull(downloadPath, "downloadPath");
        picoPath = Objects.requireNonNull(picoPath, "picoPath");
        workingDirectory = Objects.requireNonNull(workingDirectory, "workingDirectory");
        version = Objects.requireNonNull(version, "version");
        schematic = schematic == null ? "" : schematic;
        welcomeMessage = welcomeMessage == null ? "" : welcomeMessage;
        actionBar = actionBar == null ? "" : actionBar;
        defaultGameMode = Objects.requireNonNull(defaultGameMode, "defaultGameMode");
        dimension = Objects.requireNonNull(dimension, "dimension");
        spawnPosition = Objects.requireNonNull(spawnPosition, "spawnPosition").clone();
        spawnRotation = Objects.requireNonNull(spawnRotation, "spawnRotation").clone();
        forwardingMethod = Objects.requireNonNull(forwardingMethod, "forwardingMethod");
        forwardingSecret = forwardingSecret == null ? "" : forwardingSecret;
        if (version.isBlank()) {
            throw new IllegalArgumentException("PicoLimbo version must not be blank");
        }
        if (spawnPosition.length != 3) {
            throw new IllegalArgumentException("PicoLimbo spawnPosition must contain exactly 3 numbers");
        }
        if (spawnRotation.length != 2) {
            throw new IllegalArgumentException("PicoLimbo spawnRotation must contain exactly 2 numbers");
        }
        if (viewDistance < 1) {
            throw new IllegalArgumentException("PicoLimbo viewDistance must be at least 1");
        }
    }

    @Override
    public double[] spawnPosition() {
        return spawnPosition.clone();
    }

    @Override
    public double[] spawnRotation() {
        return spawnRotation.clone();
    }
}

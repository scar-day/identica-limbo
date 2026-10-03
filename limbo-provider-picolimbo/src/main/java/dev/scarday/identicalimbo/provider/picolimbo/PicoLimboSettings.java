package dev.scarday.identicalimbo.provider.picolimbo;

import java.nio.file.Path;

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
}

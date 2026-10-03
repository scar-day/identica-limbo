package dev.scarday.identicalimbo.provider.picolimbo;

import dev.scarday.identicalimbo.api.VirtualServerDefinition;
import dev.scarday.identicalimbo.provider.picolimbo.config.PicoLimboDocument;
import dev.scarday.identicalimbo.provider.picolimbo.config.PicoLimboForwarding;
import dev.scarday.identicalimbo.provider.picolimbo.config.PicoLimboLifecycle;
import dev.scarday.identicalimbo.provider.picolimbo.config.PicoLimboPaths;
import dev.scarday.identicalimbo.provider.picolimbo.config.PicoLimboRelease;
import dev.scarday.identicalimbo.provider.picolimbo.config.PicoLimboSpawn;
import dev.scarday.identicalimbo.provider.picolimbo.config.PicoLimboWorld;
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
    public static PicoLimboSettings from(PicoLimboDocument pico, VirtualServerDefinition definition) {
        PicoLimboPaths paths = pico.getPaths();
        PicoLimboLifecycle lifecycle = pico.getLifecycle();
        PicoLimboRelease release = pico.getRelease();
        PicoLimboWorld world = pico.getWorld();
        PicoLimboSpawn spawn = world.getSpawn();
        PicoLimboForwarding forwarding = pico.getForwarding();

        String workingDirectoryValue = paths.getWorkingDirectory();
        Path workingDirectory = workingDirectoryValue == null || workingDirectoryValue.isBlank()
                ? Path.of(System.getProperty("java.io.tmpdir"), "identica-limbo", "picolimbo", definition.name())
                : Path.of(workingDirectoryValue).toAbsolutePath().normalize();

        return new PicoLimboSettings(
                paths.getDownload(),
                paths.getExecutable(),
                workingDirectory,
                lifecycle.isAutoStart(),
                lifecycle.isAutoDownload(),
                release.getVersion(),
                world.getSchematic(),
                world.getWelcomeMessage(),
                world.getActionBar(),
                world.getGameMode(),
                world.getDimension(),
                spawn.parsePosition(),
                spawn.parseRotation(),
                world.getViewDistance(),
                world.isLockTime(),
                forwarding.getMethod(),
                forwarding.getSecret(),
                pico.isLogging()
        );
    }
}

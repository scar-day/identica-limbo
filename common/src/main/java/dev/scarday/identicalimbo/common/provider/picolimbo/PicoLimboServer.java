package dev.scarday.identicalimbo.common.provider.picolimbo;

import dev.scarday.identicalimbo.api.VirtualServerDefinition;
import dev.scarday.identicalimbo.api.provider.ManagedLimboServer;
import dev.scarday.identicalimbo.common.PicoLimboSettings;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class PicoLimboServer implements ManagedLimboServer {
    private final VirtualServerDefinition definition;
    private final PicoLimboSettings settings;
    private Process process;
    private Instant startedAt;

    public PicoLimboServer(VirtualServerDefinition definition, PicoLimboSettings settings, Process process, Instant startedAt) {
        this.definition = definition;
        this.settings = settings;
        this.process = process;
        this.startedAt = startedAt;
    }

    @Override
    public VirtualServerDefinition definition() {
        return definition;
    }

    public PicoLimboSettings settings() {
        return settings;
    }

    @Override
    public boolean isRunning() {
        return process != null && process.isAlive();
    }

    @Override
    public Optional<Long> pid() {
        return isRunning() ? Optional.of(process.pid()) : Optional.empty();
    }

    @Override
    public long memoryBytes() {
        if (!isRunning()) {
            return -1L;
        }
        return readProcessMemoryBytes(process);
    }

    @Override
    public Instant startedAt() {
        return isRunning() ? startedAt : null;
    }

    @Override
    public String dimension() {
        return settings != null ? settings.dimension() : null;
    }

    @Override
    public String gameMode() {
        return settings != null ? settings.defaultGameMode() : null;
    }

    @Override
    public String schematic() {
        return settings != null ? settings.schematic() : null;
    }

    @Override
    public void close() {
        Process processToClose = process;
        process = null;
        startedAt = null;
        closeProcess(processToClose);
    }

    public static long readProcessMemoryBytes(Process process) {
        if (process == null || !process.isAlive()) {
            return -1L;
        }
        long pid = process.pid();
        Path path = Path.of("/proc", String.valueOf(pid), "status");
        if (Files.isRegularFile(path)) {
            try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("VmRSS:")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 2) {
                            return Long.parseLong(parts[1]) * 1024L;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return -1L;
    }

    private static void closeProcess(Process processToClose) {
        if (processToClose == null) {
            return;
        }
        processToClose.destroy();
        try {
            if (!processToClose.waitFor(5, TimeUnit.SECONDS)) {
                processToClose.destroyForcibly();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            processToClose.destroyForcibly();
        }
    }
}

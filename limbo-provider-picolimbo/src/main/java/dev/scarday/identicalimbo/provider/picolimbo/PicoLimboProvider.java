package dev.scarday.identicalimbo.provider.picolimbo;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.scarday.identicalimbo.api.LimboProviderType;
import dev.scarday.identicalimbo.api.VirtualServerDefinition;
import dev.scarday.identicalimbo.api.provider.LimboProvider;
import dev.scarday.identicalimbo.api.provider.LimboServerContext;
import dev.scarday.identicalimbo.api.provider.ManagedLimboServer;
import dev.scarday.identicalimbo.common.config.VirtualServerDefaults;
import dev.scarday.identicalimbo.common.config.VirtualServerDocument;
import dev.scarday.identicalimbo.common.logging.LimboLogger;
import dev.scarday.identicalimbo.provider.picolimbo.config.PicoLimboDocument;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public class PicoLimboProvider implements LimboProvider {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        VirtualServerDefaults.registerContributor(doc -> {
            if (doc.getExtensions() != null) {
                doc.getExtensions().putIfAbsent("picolimbo", new PicoLimboDocument());
            }
        });
    }

    private final LimboLogger logger;
    private final PicoLimboDownloader downloader;

    public PicoLimboProvider(LimboLogger logger, PicoLimboDownloader downloader) {
        this.logger = logger;
        this.downloader = downloader;
    }

    public PicoLimboProvider(LimboLogger logger) {
        this(logger, new PicoLimboDownloader(logger));
    }

    @Override
    public LimboProviderType type() {
        return LimboProviderType.PICOLIMBO;
    }

    public static PicoLimboDocument resolveDocument(Object rawDoc) {
        if (rawDoc instanceof VirtualServerDocument doc) {
            Object rawPico = doc.getExtensions().get("picolimbo");
            if (rawPico instanceof PicoLimboDocument pico) {
                return pico;
            }
            if (rawPico != null) {
                return MAPPER.convertValue(rawPico, PicoLimboDocument.class);
            }
        }
        return new PicoLimboDocument();
    }

    @Override
    public ManagedLimboServer createServer(VirtualServerDefinition definition, LimboServerContext context)
            throws Exception {
        PicoLimboDocument pico = resolveDocument(context.document());
        pico.validate();

        PicoLimboDocument.Paths paths = pico.getPaths();
        PicoLimboDocument.Lifecycle lifecycle = pico.getLifecycle();
        PicoLimboDocument.Release release = pico.getRelease();
        PicoLimboDocument.World world = pico.getWorld();
        PicoLimboDocument.Spawn spawn = world.getSpawn();
        PicoLimboDocument.Forwarding forwarding = pico.getForwarding();

        String workingDirectoryValue = paths.getWorkingDirectory();
        Path workingDirectory = workingDirectoryValue == null || workingDirectoryValue.isBlank()
                ? Path.of(System.getProperty("java.io.tmpdir"), "identica-limbo", "picolimbo", definition.name())
                : Path.of(workingDirectoryValue).toAbsolutePath().normalize();

        PicoLimboSettings settings = new PicoLimboSettings(
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

        Process startedProcess = null;
        Instant startedAt = null;
        try {
            Path executable = downloader.ensureExecutable(settings);
            Files.createDirectories(workingDirectory);
            Path configPath = workingDirectory.resolve("server.toml").toAbsolutePath().normalize();
            String schematic = downloader.ensureSchematic(settings, workingDirectory);
            writeConfig(configPath, definition, settings, schematic);

            if (settings.autoStart()) {
                startedProcess = startProcess(executable.toAbsolutePath().normalize(), configPath, workingDirectory, settings.logging());
                startedAt = Instant.now();
            }

            context.registrar().register(definition);
            context.logInfo("Registered managed PicoLimbo {} at {}:{}", definition.name(), definition.host(), definition.port());

            return new PicoLimboServer(definition, settings, startedProcess, startedAt);
        } catch (Exception exception) {
            if (startedProcess != null) {
                startedProcess.destroyForcibly();
            }
            throw exception;
        }
    }

    private Process startProcess(Path executable, Path config, Path workingDirectory, boolean logging) throws IOException {
        Process startedProcess = new ProcessBuilder(executable.toString(), "--config", config.toString())
                .directory(workingDirectory.toFile())
                .redirectErrorStream(true)
                .start();
        Thread.ofVirtual().name("identica-limbo-pico-output").start(() -> {
            try (var input = startedProcess.getInputStream()) {
                if (logging) {
                    try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(input, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            logger.info("[PicoLimbo] {}", line);
                        }
                    }
                } else {
                    input.transferTo(java.io.OutputStream.nullOutputStream());
                }
            } catch (IOException | java.io.UncheckedIOException exception) {
                if (logging) {
                    logger.debug("PicoLimbo output closed");
                }
            }
        });
        return startedProcess;
    }

    private static void writeConfig(
            Path path, VirtualServerDefinition definition, PicoLimboSettings settings, String schematic
    ) throws IOException {
        StringBuilder toml = new StringBuilder()
                .append("bind = ").append(quote(definition.host() + ":" + definition.port())).append("\n")
                .append("welcome_message = ").append(quote(settings.welcomeMessage())).append("\n")
                .append("action_bar = ").append(quote(settings.actionBar())).append("\n")
                .append("default_game_mode = ").append(quote(settings.defaultGameMode())).append("\n\n")
                .append("[world]\n")
                .append("dimension = ").append(quote(settings.dimension())).append("\n")
                .append("spawn_position = ").append(array(settings.spawnPosition())).append("\n")
                .append("spawn_rotation = ").append(array(settings.spawnRotation())).append("\n\n")
                .append("[world.experimental]\n")
                .append("view_distance = ").append(settings.viewDistance()).append("\n")
                .append("lock_time = ").append(settings.lockTime()).append("\n")
                .append("schematic_file = ").append(quote(schematic)).append("\n\n")
                .append("[forwarding]\n")
                .append("method = ").append(quote(settings.forwardingMethod())).append("\n");
        if (settings.forwardingMethod().equalsIgnoreCase("MODERN")) {
            toml.append("secret = ").append(quote(settings.forwardingSecret())).append("\n");
        } else if (settings.forwardingMethod().equalsIgnoreCase("BUNGEE_GUARD")) {
            toml.append("tokens = [").append(quote(settings.forwardingSecret())).append("]\n");
        }
        Files.writeString(path, toml, StandardCharsets.UTF_8);
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n") + "\"";
    }

    private static String array(double[] values) {
        StringBuilder result = new StringBuilder("[");
        for (int index = 0; index < values.length; index++) {
            if (index > 0) {
                result.append(", ");
            }
            result.append(values[index]);
        }
        return result.append(']').toString();
    }
}

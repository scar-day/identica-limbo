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
import dev.scarday.identicalimbo.provider.picolimbo.process.PicoLimboConfigWriter;
import dev.scarday.identicalimbo.provider.picolimbo.process.PicoLimboProcessLauncher;
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

        PicoLimboSettings settings = PicoLimboSettings.from(pico, definition);
        Path workingDirectory = settings.workingDirectory();

        Process startedProcess = null;
        Instant startedAt = null;
        try {
            Path executable = downloader.ensureExecutable(settings);
            Files.createDirectories(workingDirectory);
            Path configPath = workingDirectory.resolve("server.toml").toAbsolutePath().normalize();
            String schematic = downloader.ensureSchematic(settings, workingDirectory);
            PicoLimboConfigWriter.writeConfig(configPath, definition, settings, schematic);

            if (settings.autoStart()) {
                startedProcess = PicoLimboProcessLauncher.startProcess(
                        executable.toAbsolutePath().normalize(),
                        configPath,
                        workingDirectory,
                        settings.logging(),
                        logger
                );
                startedAt = Instant.now();
            }

            context.registrar().register(definition);
            context.logInfo("Registered managed PicoLimbo {} at {}:{}",
                    definition.name(), definition.host(), definition.port());

            return new PicoLimboServer(definition, settings, startedProcess, startedAt);
        } catch (Exception exception) {
            if (startedProcess != null) {
                startedProcess.destroyForcibly();
            }
            throw exception;
        }
    }
}

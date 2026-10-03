package dev.scarday.identicalimbo.provider.picolimbo.process;

import dev.scarday.identicalimbo.common.logging.LimboLogger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class PicoLimboProcessLauncher {
    private PicoLimboProcessLauncher() {
    }

    public static Process startProcess(
            Path executable,
            Path config,
            Path workingDirectory,
            boolean logging,
            LimboLogger logger
    ) throws IOException {
        Process startedProcess = new ProcessBuilder(executable.toString(), "--config", config.toString())
                .directory(workingDirectory.toFile())
                .redirectErrorStream(true)
                .start();

        Thread.ofVirtual().name("identica-limbo-pico-output").start(() -> {
            try (var input = startedProcess.getInputStream()) {
                if (logging) {
                    try (var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            logger.info("[PicoLimbo] {}", line);
                        }
                    }
                } else {
                    input.transferTo(OutputStream.nullOutputStream());
                }
            } catch (IOException exception) {
                if (logging) {
                    logger.debug("PicoLimbo output closed");
                }
            }
        });

        return startedProcess;
    }
}

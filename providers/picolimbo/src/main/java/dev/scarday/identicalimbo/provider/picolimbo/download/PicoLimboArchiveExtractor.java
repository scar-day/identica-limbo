package dev.scarday.identicalimbo.provider.picolimbo.download;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class PicoLimboArchiveExtractor {
    private PicoLimboArchiveExtractor() {
    }

    public static void extractExecutable(Path archive, Path destination, String executableName)
            throws IOException, InterruptedException {
        if (archive.getFileName().toString().endsWith(".zip")) {
            extractZip(archive, destination, executableName);
            return;
        }
        extractTar(archive, destination, executableName);
    }

    private static void extractTar(Path archive, Path destination, String executableName)
            throws IOException, InterruptedException {
        Process extraction = new ProcessBuilder(
                "tar", "-xzf", archive.toString(), "-C", archive.getParent().toString()
        ).redirectErrorStream(true).start();

        if (extraction.waitFor() != 0) {
            throw new IOException("Unable to extract PicoLimbo archive with tar");
        }

        try (var paths = Files.walk(archive.getParent())) {
            Path extracted = paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(executableName))
                    .findFirst()
                    .orElseThrow(() -> new IOException("PicoLimbo executable was not found in archive"));
            if (!extracted.equals(destination)) {
                Files.move(extracted, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static void extractZip(Path archive, Path destination, String executableName) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(archive))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith(executableName)) {
                    Files.copy(zip, destination, StandardCopyOption.REPLACE_EXISTING);
                    return;
                }
            }
        }
        throw new IOException("PicoLimbo executable was not found in archive");
    }
}

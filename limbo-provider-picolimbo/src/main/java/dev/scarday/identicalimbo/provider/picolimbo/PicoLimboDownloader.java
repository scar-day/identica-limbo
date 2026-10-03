package dev.scarday.identicalimbo.provider.picolimbo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.scarday.identicalimbo.common.logging.LimboLogger;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PicoLimboDownloader {
    private static final URI RELEASES = URI.create("https://api.github.com/repos/Quozul/PicoLimbo/releases");
    private static final URI DEFAULT_SCHEMATIC = URI.create(
            "https://raw.githubusercontent.com/LOOHP/Limbo/master/spawn.schem"
    );
    private static final String DEFAULT_SCHEMATIC_NAME = "spawn.schem";
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final LimboLogger logger;

    public PicoLimboDownloader(LimboLogger logger) {
        this.logger = logger;
    }

    public Path ensureExecutable(PicoLimboSettings settings) throws IOException, InterruptedException {
        Path installationDirectory = settings.downloadPath().isBlank()
                ? settings.workingDirectory()
                : Path.of(settings.downloadPath()).toAbsolutePath().normalize();
        Path executable = settings.picoPath().isBlank()
                ? installationDirectory.resolve(executableName())
                : Path.of(settings.picoPath()).toAbsolutePath().normalize();
        if (Files.isExecutable(executable)) {
            return executable;
        }
        if (!settings.autoDownload()) {
            throw new IOException("PicoLimbo executable is missing: " + executable);
        }
        Files.createDirectories(executable.getParent() == null ? installationDirectory : executable.getParent());
        Files.createDirectories(installationDirectory);

        JsonNode release = fetchRelease(settings.version());
        JsonNode asset = findAsset(release);
        if (asset == null) {
            throw new IOException("No PicoLimbo asset for current platform and architecture");
        }
        Path archive = installationDirectory.resolve(asset.path("name").asText());
        download(asset.path("browser_download_url").asText(), asset.path("digest").asText(), archive);
        extractExecutable(archive, executable);
        Files.deleteIfExists(archive);
        executable.toFile().setExecutable(true, false);
        logger.info("Downloaded PicoLimbo {} to {}", release.path("tag_name").asText(), executable);
        return executable;
    }

    public String ensureSchematic(PicoLimboSettings settings, Path workingDirectory)
            throws IOException, InterruptedException {
        if (!settings.schematic().isBlank()) {
            Path configured = Path.of(settings.schematic());
            Path resolved = configured.isAbsolute()
                    ? configured.normalize()
                    : workingDirectory.resolve(configured).normalize();
            if (!Files.isRegularFile(resolved)) {
                throw new IOException("PicoLimbo schematic is missing: " + resolved);
            }
            return settings.schematic();
        }

        Path defaultSchematic = workingDirectory.resolve(DEFAULT_SCHEMATIC_NAME);
        if (Files.isRegularFile(defaultSchematic)) {
            return DEFAULT_SCHEMATIC_NAME;
        }
        if (!settings.autoDownload()) {
            throw new IOException("Default PicoLimbo schematic is missing: " + defaultSchematic);
        }
        downloadFile(DEFAULT_SCHEMATIC, defaultSchematic);
        logger.info("Downloaded default Limbo schematic to {}", defaultSchematic);
        return DEFAULT_SCHEMATIC_NAME;
    }

    private JsonNode fetchRelease(String version) throws IOException, InterruptedException {
        URI uri = "latest".equalsIgnoreCase(version)
                ? URI.create(RELEASES + "/latest")
                : URI.create(RELEASES + "/tags/" + version);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "identica-limbo")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IOException("GitHub release request failed with HTTP " + response.statusCode());
        }
        return mapper.readTree(response.body());
    }

    private JsonNode findAsset(JsonNode release) {
        String wanted = assetName();
        for (JsonNode asset : release.path("assets")) {
            if (asset.path("name").asText().equals(wanted)) {
                return asset;
            }
        }
        return null;
    }

    private void download(String url, String digest, Path destination) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMinutes(5))
                .header("User-Agent", "identica-limbo")
                .GET()
                .build();
        Path temporary = destination.resolveSibling(destination.getFileName() + ".part");
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() / 100 != 2) {
            response.body().close();
            throw new IOException("PicoLimbo download failed with HTTP " + response.statusCode());
        }
        try (InputStream input = response.body(); var output = Files.newOutputStream(temporary)) {
            input.transferTo(output);
        }
        if (!digest.isBlank()) {
            verifyDigest(temporary, digest);
        }
        Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private void downloadFile(URI source, Path destination) throws IOException, InterruptedException {
        Files.createDirectories(destination.getParent());
        HttpRequest request = HttpRequest.newBuilder(source)
                .timeout(Duration.ofMinutes(5))
                .header("User-Agent", "identica-limbo")
                .GET()
                .build();
        Path temporary = destination.resolveSibling(destination.getFileName() + ".part");
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() / 100 != 2) {
            throw new IOException("Limbo schematic download failed with HTTP " + response.statusCode());
        }
        try (InputStream input = response.body(); var output = Files.newOutputStream(temporary)) {
            input.transferTo(output);
        }
        Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private static void verifyDigest(Path file, String digest) throws IOException {
        if (!digest.startsWith("sha256:")) {
            return;
        }
        try (InputStream input = Files.newInputStream(file)) {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            input.transferTo(new java.io.OutputStream() {
                @Override
                public void write(int value) {
                    sha256.update((byte) value);
                }

                @Override
                public void write(byte[] bytes, int offset, int length) {
                    sha256.update(bytes, offset, length);
                }
            });
            String actual = HexFormat.of().formatHex(sha256.digest());
            String expected = digest.substring("sha256:".length());
            if (!actual.equalsIgnoreCase(expected)) {
                throw new IOException("PicoLimbo archive checksum mismatch");
            }
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IOException("SHA-256 is unavailable", exception);
        }
    }

    private static void extractExecutable(Path archive, Path destination) throws IOException, InterruptedException {
        if (archive.getFileName().toString().endsWith(".zip")) {
            extractZip(archive, destination);
            return;
        }
        Process extraction = new ProcessBuilder(
                "tar", "-xzf", archive.toString(), "-C", archive.getParent().toString()
        ).redirectErrorStream(true).start();
        if (extraction.waitFor() != 0) {
            throw new IOException("Unable to extract PicoLimbo archive with tar");
        }
        try (var paths = Files.walk(archive.getParent())) {
            Path extracted = paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(executableName()))
                    .findFirst()
                    .orElseThrow(() -> new IOException("PicoLimbo executable was not found in archive"));
            if (!extracted.equals(destination)) {
                Files.move(extracted, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static void extractZip(Path archive, Path destination) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(archive))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith(executableName())) {
                    Files.copy(zip, destination, StandardCopyOption.REPLACE_EXISTING);
                    return;
                }
            }
        }
        throw new IOException("PicoLimbo executable was not found in archive");
    }

    private static String assetName() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
        if (os.contains("linux") && (arch.equals("amd64") || arch.equals("x86_64"))) {
            return "pico_limbo_linux-x86_64-gnu.tar.gz";
        }
        if (os.contains("linux") && (arch.equals("aarch64") || arch.equals("arm64"))) {
            return "pico_limbo_linux-aarch64-gnu.tar.gz";
        }
        if (os.contains("windows") && (arch.equals("amd64") || arch.equals("x86_64"))) {
            return "pico_limbo_windows-x86_64.zip";
        }
        if (os.contains("mac") && (arch.equals("aarch64") || arch.equals("arm64"))) {
            return "pico_limbo_macos-aarch64.tar.gz";
        }
        throw new IllegalStateException("Unsupported PicoLimbo platform: " + os + "/" + arch);
    }

    private static String executableName() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")
                ? "pico_limbo.exe"
                : "pico_limbo";
    }
}

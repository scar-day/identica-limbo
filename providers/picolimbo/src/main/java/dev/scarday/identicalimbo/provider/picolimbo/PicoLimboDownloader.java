package dev.scarday.identicalimbo.provider.picolimbo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.scarday.identicalimbo.common.logging.LimboLogger;
import dev.scarday.identicalimbo.provider.picolimbo.download.ChecksumVerifier;
import dev.scarday.identicalimbo.provider.picolimbo.download.PicoLimboArchiveExtractor;
import dev.scarday.identicalimbo.provider.picolimbo.download.PicoLimboPlatform;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

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
                ? installationDirectory.resolve(PicoLimboPlatform.executableName())
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
        JsonNode asset = PicoLimboPlatform.findAsset(release);
        if (asset == null) {
            throw new IOException("No PicoLimbo asset for current platform and architecture");
        }
        Path archive = installationDirectory.resolve(asset.path("name").asText());
        download(asset.path("browser_download_url").asText(), asset.path("digest").asText(), archive);
        PicoLimboArchiveExtractor.extractExecutable(archive, executable, PicoLimboPlatform.executableName());
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

    private void download(String url, String digest, Path destination) throws IOException, InterruptedException {
        downloadFile(URI.create(url), destination);
        if (!digest.isBlank()) {
            ChecksumVerifier.verifySha256(destination, digest);
        }
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
            throw new IOException("Download failed with HTTP " + response.statusCode() + " from " + source);
        }
        try (InputStream input = response.body(); var output = Files.newOutputStream(temporary)) {
            input.transferTo(output);
        }
        Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
}

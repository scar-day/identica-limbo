package dev.scarday.identicalimbo.provider.picolimbo.download;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Locale;

public final class PicoLimboPlatform {
    public static String assetName() {
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

    public static String executableName() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")
                ? "pico_limbo.exe"
                : "pico_limbo";
    }

    public static JsonNode findAsset(JsonNode release) {
        String wanted = assetName();
        for (JsonNode asset : release.path("assets")) {
            if (asset.path("name").asText().equals(wanted)) {
                return asset;
            }
        }
        return null;
    }
}

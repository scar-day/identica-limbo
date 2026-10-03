package dev.scarday.identicalimbo.provider.picolimbo.process;

import dev.scarday.identicalimbo.api.VirtualServerDefinition;
import dev.scarday.identicalimbo.provider.picolimbo.PicoLimboSettings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PicoLimboConfigWriter {
    private PicoLimboConfigWriter() {
    }

    public static void writeConfig(
            Path path,
            VirtualServerDefinition definition,
            PicoLimboSettings settings,
            String schematic
    ) throws IOException {
        StringBuilder toml = new StringBuilder()
                .append("bind = ").append(quote(definition.host() + ":" + definition.port())).append("\n")
                .append("welcome_message = ").append(quote(settings.welcomeMessage())).append("\n")
                .append("action_bar = ").append(quote(settings.actionBar())).append("\n")
                .append("default_game_mode = ").append(quote(settings.defaultGameMode().getId())).append("\n\n")
                .append("[world]\n")
                .append("dimension = ").append(quote(settings.dimension().getId())).append("\n")
                .append("spawn_position = ").append(array(settings.spawnPosition())).append("\n")
                .append("spawn_rotation = ").append(array(settings.spawnRotation())).append("\n\n")
                .append("[world.experimental]\n")
                .append("view_distance = ").append(settings.viewDistance()).append("\n")
                .append("lock_time = ").append(settings.lockTime()).append("\n")
                .append("schematic_file = ").append(quote(schematic)).append("\n\n")
                .append("[forwarding]\n")
                .append("method = ").append(quote(settings.forwardingMethod().getId())).append("\n");

        switch (settings.forwardingMethod()) {
            case MODERN -> toml.append("secret = ").append(quote(settings.forwardingSecret())).append("\n");
            case BUNGEE_GUARD -> toml.append("tokens = [").append(quote(settings.forwardingSecret())).append("]\n");
            case NONE -> {}
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

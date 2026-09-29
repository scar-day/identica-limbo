package dev.scarday.identicalimbo.command.info;

import dev.scarday.identicalimbo.api.VirtualServerDefinition;
import dev.scarday.identicalimbo.api.VirtualServerStatus;
import dev.scarday.identicalimbo.common.config.LimboCommandMessages;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public final class LimboInfoFormatter {
    LimboStatusFormatter statusFormatter;
    LimboAddressFormatter addressFormatter;
    LimboPlayerFormatter playerFormatter;
    LimboMemoryFormatter memoryFormatter;
    LimboDurationFormatter durationFormatter;
    LimboDetailFormatter detailFormatter;

    public LimboInfoFormatter() {
        this(
                new LimboStatusFormatter(),
                new LimboAddressFormatter(),
                new LimboPlayerFormatter(),
                new LimboMemoryFormatter(),
                new LimboDurationFormatter(),
                new LimboDetailFormatter()
        );
    }

    public Map<String, String> formatPlaceholders(VirtualServerStatus status, LimboCommandMessages messages) {
        VirtualServerDefinition definition = status.definition();
        String none = messages.getDetails().getNone();

        Map<String, String> placeholders = new LinkedHashMap<>();
        placeholders.put("name", definition.name());
        placeholders.put("provider", definition.provider().name());
        placeholders.put("host", definition.host());
        placeholders.put("port", String.valueOf(definition.port()));
        placeholders.put("address", addressFormatter.formatAddress(status, messages.getAddress()));
        placeholders.put("status", statusFormatter.formatStatus(status, messages.getStatus()));
        placeholders.put("raw_status", statusFormatter.rawStatus(status, messages.getStatus()));
        placeholders.put("players", playerFormatter.formatPlayers(status, messages.getPlayers()));
        placeholders.put("online", String.valueOf(status.onlinePlayers()));
        placeholders.put("players_count", String.valueOf(status.onlinePlayers()));
        placeholders.put("players_list", playerFormatter.formatPlayerList(status, messages.getPlayers()));
        placeholders.put("memory", memoryFormatter.formatMemory(status, messages.getMemory()));
        placeholders.put("uptime", durationFormatter.formatDuration(status.startedAt(), status.running(), messages.getDuration(), none));
        placeholders.put("pid", status.pid() != null ? String.valueOf(status.pid()) : none);
        placeholders.put("dimension", status.dimension() != null ? status.dimension() : none);
        placeholders.put("gamemode", status.gameMode() != null ? status.gameMode() : none);
        placeholders.put("schematic", status.schematic() != null ? status.schematic() : none);
        placeholders.put("details", detailFormatter.formatDetails(status, messages.getDetails()));
        return placeholders;
    }
}

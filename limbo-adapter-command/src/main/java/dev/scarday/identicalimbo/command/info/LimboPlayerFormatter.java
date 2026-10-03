package dev.scarday.identicalimbo.command.info;

import dev.scarday.identicalimbo.api.VirtualServerStatus;
import dev.scarday.identicalimbo.common.config.LimboPlayerMessages;

public final class LimboPlayerFormatter {
    private static final String COUNT_PLACEHOLDER = "{count}";
    private static final String ONLINE_PLACEHOLDER = "{online}";
    private static final String NAMES_PLACEHOLDER = "{names}";
    private static final String DELIMITER = ", ";

    public String formatPlayers(VirtualServerStatus status, LimboPlayerMessages messages) {
        int online = status.onlinePlayers();
        if (online <= 0) return messages.getEmpty();

        if (status.playerNames() != null && !status.playerNames().isEmpty()) {
            String names = String.join(DELIMITER, status.playerNames());
            return messages.getWithNames()
                    .replace(COUNT_PLACEHOLDER, String.valueOf(online))
                    .replace(ONLINE_PLACEHOLDER, String.valueOf(online))
                    .replace(NAMES_PLACEHOLDER, names);
        }
        return String.valueOf(online);
    }

    public String formatPlayerList(VirtualServerStatus status, LimboPlayerMessages messages) {
        if (status.playerNames() != null && !status.playerNames().isEmpty()) return String.join(DELIMITER, status.playerNames());
        return messages.getListEmpty();
    }
}

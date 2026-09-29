package dev.scarday.identicalimbo.command.info;

import dev.scarday.identicalimbo.api.VirtualServerStatus;
import dev.scarday.identicalimbo.common.config.LimboDetailMessages;

public final class LimboDetailFormatter {
    private static final String DIMENSION_PLACEHOLDER = "{dimension}";
    private static final String GAMEMODE_PLACEHOLDER = "{gamemode}";
    private static final String SCHEMATIC_PLACEHOLDER = "{schematic}";

    public String formatDetails(VirtualServerStatus status, LimboDetailMessages messages) {
        StringBuilder builder = new StringBuilder();
        if (status.dimension() != null && !status.dimension().isBlank()) {
            builder.append(messages.getDimension().replace(DIMENSION_PLACEHOLDER, status.dimension()));
        }
        if (status.gameMode() != null && !status.gameMode().isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(messages.getSeparator());
            }
            builder.append(messages.getGameMode().replace(GAMEMODE_PLACEHOLDER, status.gameMode()));
        }
        if (status.schematic() != null && !status.schematic().isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(messages.getSeparator());
            }
            builder.append(messages.getSchematic().replace(SCHEMATIC_PLACEHOLDER, status.schematic()));
        }
        return builder.isEmpty() ? messages.getNone() : builder.toString();
    }
}

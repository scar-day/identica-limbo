package dev.scarday.identicalimbo.command.info;

import dev.scarday.identicalimbo.api.VirtualServerStatus;
import dev.scarday.identicalimbo.common.config.LimboAddressMessages;

public final class LimboAddressFormatter {
    private static final String PORT_SEPARATOR = ":";

    public String formatAddress(VirtualServerStatus status, LimboAddressMessages messages) {
        if (status.port() <= 0) {
            return messages.getVirtual();
        }
        return status.host() + PORT_SEPARATOR + status.port();
    }
}

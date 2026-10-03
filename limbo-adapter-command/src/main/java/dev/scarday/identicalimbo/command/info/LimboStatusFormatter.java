package dev.scarday.identicalimbo.command.info;

import dev.scarday.identicalimbo.api.VirtualServerStatus;
import dev.scarday.identicalimbo.common.config.LimboStatusMessages;

public final class LimboStatusFormatter {
    private static final String PID_PLACEHOLDER = "{pid}";

    public String formatStatus(VirtualServerStatus status, LimboStatusMessages messages) {
        if (status.running()) {
            if (status.pid() != null) return messages.getRunningWithPid().replace(PID_PLACEHOLDER, String.valueOf(status.pid()));
            return messages.getRunning();
        }
        return messages.getStopped();
    }

    public String rawStatus(VirtualServerStatus status, LimboStatusMessages messages) {
        if (status.running()) return messages.getRawRunning();
        return messages.getRawStopped();
    }
}

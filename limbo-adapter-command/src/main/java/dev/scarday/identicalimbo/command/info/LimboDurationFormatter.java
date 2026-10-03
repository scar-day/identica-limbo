package dev.scarday.identicalimbo.command.info;

import dev.scarday.identicalimbo.common.config.LimboDurationMessages;

import java.time.Duration;
import java.time.Instant;

public final class LimboDurationFormatter {
    private static final long SECONDS_PER_MINUTE = 60L;
    private static final long SECONDS_PER_HOUR = 3600L;
    private static final long SECONDS_PER_DAY = 86400L;

    public String formatDuration(Instant startedAt, boolean running, LimboDurationMessages messages, String fallback) {
        if (!running || startedAt == null) return fallback;

        Duration duration = Duration.between(startedAt, Instant.now());
        if (duration.isNegative() || duration.isZero()) return messages.getZero();

        long totalSeconds = duration.toSeconds();
        long days = totalSeconds / SECONDS_PER_DAY;
        long hours = (totalSeconds % SECONDS_PER_DAY) / SECONDS_PER_HOUR;
        long minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE;
        long seconds = totalSeconds % SECONDS_PER_MINUTE;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(messages.getDays());
        if (hours > 0) sb.append(hours).append(messages.getHours());
        if (minutes > 0) sb.append(minutes).append(messages.getMinutes());
        if (seconds > 0 || sb.isEmpty()) sb.append(seconds).append(messages.getSeconds());
        return sb.toString().trim();
    }
}

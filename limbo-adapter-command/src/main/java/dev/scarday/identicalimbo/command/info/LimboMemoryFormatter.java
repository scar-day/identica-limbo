package dev.scarday.identicalimbo.command.info;

import dev.scarday.identicalimbo.api.VirtualServerStatus;
import dev.scarday.identicalimbo.common.config.LimboMemoryMessages;
import java.util.Locale;

public final class LimboMemoryFormatter {
    private static final String VALUE_PLACEHOLDER = "{value}";
    private static final String USED_PLACEHOLDER = "{used}";
    private static final String MAX_PLACEHOLDER = "{max}";
    private static final long KILOBYTE = 1024L;
    private static final long MEGABYTE = KILOBYTE * 1024L;
    private static final long GIGABYTE = MEGABYTE * 1024L;
    private static final String ONE_DECIMAL_FORMAT = "%.1f";
    private static final String TWO_DECIMAL_FORMAT = "%.2f";

    public String formatMemory(VirtualServerStatus status, LimboMemoryMessages messages) {
        if (status.memoryBytes() < 0) {
            return messages.getNotAvailable();
        }
        String used = formatUnit(status.memoryBytes(), messages);
        if (status.maxMemoryBytes() > 0) {
            String max = formatUnit(status.maxMemoryBytes(), messages);
            return messages.getJvm()
                    .replace(USED_PLACEHOLDER, used)
                    .replace(MAX_PLACEHOLDER, max);
        }
        return used;
    }

    public String formatUnit(long bytes, LimboMemoryMessages messages) {
        if (bytes < 0) {
            return messages.getNotAvailable();
        }
        if (bytes < KILOBYTE) {
            return messages.getBytes().replace(VALUE_PLACEHOLDER, String.valueOf(bytes));
        }
        if (bytes < MEGABYTE) {
            String value = String.format(Locale.ROOT, ONE_DECIMAL_FORMAT, bytes / (double) KILOBYTE);
            return messages.getKilobytes().replace(VALUE_PLACEHOLDER, value);
        }
        if (bytes < GIGABYTE) {
            String value = String.format(Locale.ROOT, ONE_DECIMAL_FORMAT, bytes / (double) MEGABYTE);
            return messages.getMegabytes().replace(VALUE_PLACEHOLDER, value);
        }
        String value = String.format(Locale.ROOT, TWO_DECIMAL_FORMAT, bytes / (double) GIGABYTE);
        return messages.getGigabytes().replace(VALUE_PLACEHOLDER, value);
    }
}

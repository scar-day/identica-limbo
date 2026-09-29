package dev.scarday.identicalimbo.common.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class LimboMemoryMessages {
    private String bytes = "{value} B";
    private String kilobytes = "{value} KB";
    private String megabytes = "{value} MB";
    private String gigabytes = "{value} GB";
    private String jvm = "{used} / {max} (JVM)";
    private String notAvailable = "N/A";

    public void copyFrom(LimboMemoryMessages source) {
        if (source == null) return;
        if (source.bytes != null) this.bytes = source.bytes;
        if (source.kilobytes != null) this.kilobytes = source.kilobytes;
        if (source.megabytes != null) this.megabytes = source.megabytes;
        if (source.gigabytes != null) this.gigabytes = source.gigabytes;
        if (source.jvm != null) this.jvm = source.jvm;
        if (source.notAvailable != null) this.notAvailable = source.notAvailable;
    }

    public boolean populateDefaults() {
        boolean changed = false;
        if (bytes == null) { bytes = "{value} B"; changed = true; }
        if (kilobytes == null) { kilobytes = "{value} KB"; changed = true; }
        if (megabytes == null) { megabytes = "{value} MB"; changed = true; }
        if (gigabytes == null) { gigabytes = "{value} GB"; changed = true; }
        if (jvm == null) { jvm = "{used} / {max} (JVM)"; changed = true; }
        if (notAvailable == null) { notAvailable = "N/A"; changed = true; }
        return changed;
    }
}

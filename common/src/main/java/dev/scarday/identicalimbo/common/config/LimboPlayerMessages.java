package dev.scarday.identicalimbo.common.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class LimboPlayerMessages {
    private String empty = "0";
    private String listEmpty = "нет";
    private String withNames = "{count} <gray>({names})</gray>";

    public void copyFrom(LimboPlayerMessages source) {
        if (source == null) return;
        if (source.empty != null) this.empty = source.empty;
        if (source.listEmpty != null) this.listEmpty = source.listEmpty;
        if (source.withNames != null) this.withNames = source.withNames;
    }

    public boolean populateDefaults() {
        boolean changed = false;
        if (empty == null) { empty = "0"; changed = true; }
        if (listEmpty == null) { listEmpty = "нет"; changed = true; }
        if (withNames == null) { withNames = "{count} <gray>({names})</gray>"; changed = true; }
        return changed;
    }
}

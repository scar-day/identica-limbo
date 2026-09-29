package dev.scarday.identicalimbo.common.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class LimboAddressMessages {
    private String virtual = "Виртуальный (in-proxy)";

    public void copyFrom(LimboAddressMessages source) {
        if (source == null) return;
        if (source.virtual != null) this.virtual = source.virtual;
    }

    public boolean populateDefaults() {
        boolean changed = false;
        if (virtual == null) { virtual = "Виртуальный (in-proxy)"; changed = true; }
        return changed;
    }
}

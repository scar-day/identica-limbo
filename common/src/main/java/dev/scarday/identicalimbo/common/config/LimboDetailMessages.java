package dev.scarday.identicalimbo.common.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class LimboDetailMessages {
    private String dimension = "Мир: {dimension}";
    private String gameMode = "Режим: {gamemode}";
    private String schematic = "Схематика: {schematic}";
    private String separator = " | ";
    private String none = "—";

    public void copyFrom(LimboDetailMessages source) {
        if (source == null) return;
        if (source.dimension != null) this.dimension = source.dimension;
        if (source.gameMode != null) this.gameMode = source.gameMode;
        if (source.schematic != null) this.schematic = source.schematic;
        if (source.separator != null) this.separator = source.separator;
        if (source.none != null) this.none = source.none;
    }

    public boolean populateDefaults() {
        boolean changed = false;
        if (dimension == null) { dimension = "Мир: {dimension}"; changed = true; }
        if (gameMode == null) { gameMode = "Режим: {gamemode}"; changed = true; }
        if (schematic == null) { schematic = "Схематика: {schematic}"; changed = true; }
        if (separator == null) { separator = " | "; changed = true; }
        if (none == null) { none = "—"; changed = true; }
        return changed;
    }
}

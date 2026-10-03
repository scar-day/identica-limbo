package dev.scarday.identicalimbo.provider.picolimbo.config;

import dev.scarday.identicalimbo.provider.picolimbo.config.enums.Dimension;
import dev.scarday.identicalimbo.provider.picolimbo.config.enums.Gamemode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PicoLimboWorld {
    private String schematic = "";
    private String welcomeMessage = "";
    private String actionBar = "";
    private Gamemode gameMode = Gamemode.SPECTATOR;
    private Dimension dimension = Dimension.OVERWORLD;
    private PicoLimboSpawn spawn = new PicoLimboSpawn();
    private int viewDistance = 2;
    private boolean lockTime = false;

    public void validate() {
        if (spawn == null) spawn = new PicoLimboSpawn();
        spawn.parsePosition();
        spawn.parseRotation();
        if (viewDistance < 1) {
            throw new IllegalArgumentException("picolimbo.world.viewDistance must be at least 1");
        }
        if (gameMode == null) {
            throw new IllegalArgumentException("picolimbo.world.gameMode must not be null");
        }
        if (dimension == null) {
            throw new IllegalArgumentException("picolimbo.world.dimension must not be null");
        }
    }
}

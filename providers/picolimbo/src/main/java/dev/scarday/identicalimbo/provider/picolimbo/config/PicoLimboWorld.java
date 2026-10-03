package dev.scarday.identicalimbo.provider.picolimbo.config;

import java.util.Locale;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PicoLimboWorld {
    private String schematic = "";
    private String welcomeMessage = "";
    private String actionBar = "";
    private String gameMode = "spectator";
    private String dimension = "overworld";
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
        if (gameMode == null || !Set.of("survival", "creative", "adventure", "spectator")
                .contains(gameMode.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException(
                    "picolimbo.world.gameMode must be survival, creative, adventure or spectator"
            );
        }
        if (dimension == null || !Set.of("overworld", "nether", "end")
                .contains(dimension.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("picolimbo.world.dimension must be overworld, nether or end");
        }
    }
}

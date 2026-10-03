package dev.scarday.identicalimbo.provider.picolimbo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.scarday.identicalimbo.provider.picolimbo.config.enums.Dimension;
import dev.scarday.identicalimbo.provider.picolimbo.config.enums.Forwarding;
import dev.scarday.identicalimbo.provider.picolimbo.config.enums.Gamemode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class PicoLimboEnumsTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testGamemodeEnum() {
        Assertions.assertEquals(Gamemode.SURVIVAL, Gamemode.fromString("survival"));
        Assertions.assertEquals(Gamemode.SURVIVAL, Gamemode.fromString("SURVIVAL"));
        Assertions.assertEquals(Gamemode.CREATIVE, Gamemode.fromString("creative"));
        Assertions.assertEquals(Gamemode.ADVENTURE, Gamemode.fromString("adventure"));
        Assertions.assertEquals(Gamemode.SPECTATOR, Gamemode.fromString("spectator"));
        Assertions.assertEquals(Gamemode.SPECTATOR, Gamemode.fromString(null));
        Assertions.assertEquals(Gamemode.SPECTATOR, Gamemode.fromString("   "));

        Assertions.assertThrows(IllegalArgumentException.class, () -> Gamemode.fromString("hardcore"));
    }

    @Test
    public void testForwardingEnum() {
        Assertions.assertEquals(Forwarding.NONE, Forwarding.fromString("none"));
        Assertions.assertEquals(Forwarding.NONE, Forwarding.fromString("NONE"));
        Assertions.assertEquals(Forwarding.MODERN, Forwarding.fromString("modern"));
        Assertions.assertEquals(Forwarding.MODERN, Forwarding.fromString("MODERN"));
        Assertions.assertEquals(Forwarding.BUNGEE_GUARD, Forwarding.fromString("bungee_guard"));
        Assertions.assertEquals(Forwarding.BUNGEE_GUARD, Forwarding.fromString("BUNGEE_GUARD"));
        Assertions.assertEquals(Forwarding.NONE, Forwarding.fromString(null));
        Assertions.assertEquals(Forwarding.NONE, Forwarding.fromString("   "));

        Assertions.assertThrows(IllegalArgumentException.class, () -> Forwarding.fromString("velocity"));
    }

    @Test
    public void testDimensionEnum() {
        Assertions.assertEquals(Dimension.OVERWORLD, Dimension.fromString("overworld"));
        Assertions.assertEquals(Dimension.OVERWORLD, Dimension.fromString("OVERWORLD"));
        Assertions.assertEquals(Dimension.NETHER, Dimension.fromString("nether"));
        Assertions.assertEquals(Dimension.END, Dimension.fromString("end"));
        Assertions.assertEquals(Dimension.OVERWORLD, Dimension.fromString(null));
        Assertions.assertEquals(Dimension.OVERWORLD, Dimension.fromString("   "));

        Assertions.assertThrows(IllegalArgumentException.class, () -> Dimension.fromString("twilight_forest"));
    }

    @Test
    public void testJacksonDeserialization() throws Exception {
        String json = """
                {
                    "world": {
                        "gameMode": "creative",
                        "dimension": "nether"
                    },
                    "forwarding": {
                        "method": "MODERN",
                        "secret": "my-secret"
                    }
                }
                """;

        PicoLimboDocument document = mapper.readValue(json, PicoLimboDocument.class);
        document.validate();

        Assertions.assertEquals(Gamemode.CREATIVE, document.getWorld().getGameMode());
        Assertions.assertEquals(Dimension.NETHER, document.getWorld().getDimension());
        Assertions.assertEquals(Forwarding.MODERN, document.getForwarding().getMethod());
        Assertions.assertEquals("my-secret", document.getForwarding().getSecret());
    }

    @Test
    public void testJacksonAliasDeserialization() throws Exception {
        String json = """
                {
                    "forwarding": {
                        "type": "BUNGEE_GUARD",
                        "secret": "token123"
                    }
                }
                """;

        PicoLimboDocument document = mapper.readValue(json, PicoLimboDocument.class);
        Assertions.assertEquals(Forwarding.BUNGEE_GUARD, document.getForwarding().getMethod());
    }
}

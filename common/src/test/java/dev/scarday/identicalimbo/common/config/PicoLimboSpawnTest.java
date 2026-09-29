package dev.scarday.identicalimbo.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class PicoLimboSpawnTest {
    @Test
    public void testSemicolonStringCoordinates() {
        PicoLimboDocument.Spawn spawn = new PicoLimboDocument.Spawn();
        spawn.setPosition("10.5;20.0;30.5");
        spawn.setRotation("90.0;-45.5");

        double[] pos = spawn.parsePosition();
        double[] rot = spawn.parseRotation();

        Assertions.assertArrayEquals(new double[]{10.5, 20.0, 30.5}, pos, 0.001);
        Assertions.assertArrayEquals(new double[]{90.0, -45.5}, rot, 0.001);
    }

    @Test
    public void testSemicolonWithSpaces() {
        PicoLimboDocument.Spawn spawn = new PicoLimboDocument.Spawn();
        spawn.setPosition("  10.5 ; 20.0 ;  30.5 ");
        spawn.setRotation(" 90.0 ; -45.5 ");

        double[] pos = spawn.parsePosition();
        double[] rot = spawn.parseRotation();

        Assertions.assertArrayEquals(new double[]{10.5, 20.0, 30.5}, pos, 0.001);
        Assertions.assertArrayEquals(new double[]{90.0, -45.5}, rot, 0.001);
    }

    @Test
    public void testDeserializationFromString() throws Exception {
        String json = "{\"position\":\"1.0;2.0;3.0\",\"rotation\":\"4.0;5.0\"}";
        ObjectMapper mapper = new ObjectMapper();
        PicoLimboDocument.Spawn spawn = mapper.readValue(json, PicoLimboDocument.Spawn.class);

        Assertions.assertEquals("1.0;2.0;3.0", spawn.getPosition());
        Assertions.assertEquals("4.0;5.0", spawn.getRotation());
        Assertions.assertArrayEquals(new double[]{1.0, 2.0, 3.0}, spawn.parsePosition(), 0.001);
        Assertions.assertArrayEquals(new double[]{4.0, 5.0}, spawn.parseRotation(), 0.001);
    }

    @Test
    public void testDeserializationFromArrayBackwardCompatibility() throws Exception {
        String json = "{\"position\":[10.0,20.0,30.0],\"rotation\":[-90.0,0.0]}";
        ObjectMapper mapper = new ObjectMapper();
        PicoLimboDocument.Spawn spawn = mapper.readValue(json, PicoLimboDocument.Spawn.class);

        Assertions.assertEquals("10.0;20.0;30.0", spawn.getPosition());
        Assertions.assertEquals("-90.0;0.0", spawn.getRotation());
        Assertions.assertArrayEquals(new double[]{10.0, 20.0, 30.0}, spawn.parsePosition(), 0.001);
        Assertions.assertArrayEquals(new double[]{-90.0, 0.0}, spawn.parseRotation(), 0.001);
    }

    @Test
    public void testInvalidFormats() {
        PicoLimboDocument.Spawn spawn = new PicoLimboDocument.Spawn();
        spawn.setPosition("10.0;20.0");
        Assertions.assertThrows(IllegalArgumentException.class, spawn::parsePosition);

        spawn.setPosition("10.0;20.0;abc");
        Assertions.assertThrows(IllegalArgumentException.class, spawn::parsePosition);

        spawn.setRotation("0.0");
        Assertions.assertThrows(IllegalArgumentException.class, spawn::parseRotation);
    }
}

package dev.scarday.identicalimbo.provider.picolimbo.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PicoLimboSpawn {
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = SemicolonCoordinateDeserializer.class)
    private String position = "20.5;17.0;22.5";

    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = SemicolonCoordinateDeserializer.class)
    private String rotation = "-90.0;0.0";

    public double[] parsePosition() {
        return parse(position, 3, "position");
    }

    public double[] parseRotation() {
        return parse(rotation, 2, "rotation");
    }

    private static double[] parse(String raw, int expectedLength, String fieldName) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("picolimbo.world.spawn." + fieldName + " must not be blank");
        }
        String[] parts = raw.split(";");
        if (parts.length != expectedLength) {
            throw new IllegalArgumentException(
                    "picolimbo.world.spawn." + fieldName + " must contain " + expectedLength + " numbers separated by ';'"
            );
        }
        double[] result = new double[expectedLength];
        for (int i = 0; i < expectedLength; i++) {
            try {
                result[i] = Double.parseDouble(parts[i].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Invalid number in picolimbo.world.spawn." + fieldName + ": " + parts[i].trim()
                );
            }
        }
        return result;
    }
}

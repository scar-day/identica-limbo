package dev.scarday.identicalimbo.common.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class SemicolonCoordinateDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.currentToken() == JsonToken.START_ARRAY) {
            List<String> elements = new ArrayList<>();
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                elements.add(parser.getText().trim());
            }
            return String.join(";", elements);
        }
        return parser.getText().trim();
    }
}

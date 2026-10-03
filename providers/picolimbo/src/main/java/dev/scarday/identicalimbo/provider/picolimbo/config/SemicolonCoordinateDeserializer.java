package dev.scarday.identicalimbo.provider.picolimbo.config;

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
        if (parser.currentToken() == JsonToken.VALUE_STRING) {
            return parser.getText();
        }
        if (parser.currentToken() == JsonToken.START_ARRAY) {
            List<String> values = new ArrayList<>();
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                values.add(parser.getText());
            }
            return String.join(";", values);
        }
        throw context.instantiationException(String.class, "Expected string or array for coordinate format");
    }
}

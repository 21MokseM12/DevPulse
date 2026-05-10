package scrapper.bot.connectivity.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class UniqueStringSetDeserializer extends JsonDeserializer<Set<String>> {

    @Override
    public Set<String> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken currentToken = parser.currentToken();
        if (currentToken == JsonToken.VALUE_NULL) {
            return null;
        }
        if (currentToken != JsonToken.START_ARRAY) {
            throw JsonMappingException.from(parser, "Expected JSON array");
        }

        JsonNode arrayNode = parser.getCodec().readTree(parser);
        Set<String> values = new LinkedHashSet<>();
        for (JsonNode element : arrayNode) {
            if (!element.isTextual()) {
                throw JsonMappingException.from(parser, "Array elements must be strings");
            }

            String value = element.asText();
            if (!values.add(value)) {
                throw JsonMappingException.from(parser, "Duplicate elements are not allowed");
            }
        }
        return values;
    }
}

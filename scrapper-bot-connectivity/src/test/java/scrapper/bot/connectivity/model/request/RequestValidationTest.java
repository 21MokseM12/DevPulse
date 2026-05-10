package scrapper.bot.connectivity.model.request;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import org.junit.jupiter.api.Test;
import scrapper.bot.connectivity.validation.ValidUriValidator;

public class RequestValidationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ValidUriValidator validUriValidator = new ValidUriValidator();

    @Test
    void addLinkRequest_deserializationRejectsDuplicateTags() {
        String payload = "{\"link\":\"https://example.com/resource\",\"tags\":[\"backend\",\"backend\"]}";

        assertThrows(IOException.class, () -> OBJECT_MAPPER.readValue(payload, AddLinkRequest.class));
    }

    @Test
    void validUriValidator_acceptsAbsoluteUri() {
        assertTrue(validUriValidator.isValid(URI.create("https://example.com/path"), null));
    }

    @Test
    void validUriValidator_rejectsRelativeUri() {
        assertFalse(validUriValidator.isValid(URI.create("/relative/path"), null));
    }
}

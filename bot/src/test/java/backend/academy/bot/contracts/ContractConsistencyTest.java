package backend.academy.bot.contracts;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ContractConsistencyTest {

    private static final Path BOT_OPENAPI = Path.of("contracts/openapi.yaml");

    @Test
    void openapi_shouldDescribeErrorResponsesAndUpdatePayload() throws IOException {
        String openapi = Files.readString(BOT_OPENAPI);

        assertTrue(openapi.contains("/api/v1/clients"));
        assertTrue(openapi.contains("'400':"));
        assertTrue(openapi.contains("'404':"));
        assertTrue(openapi.contains("ApiErrorResponse"));
        assertTrue(openapi.contains("/updates"));
        assertTrue(openapi.contains("LinkUpdate"));
        assertTrue(openapi.contains("clientsIds"));
    }
}

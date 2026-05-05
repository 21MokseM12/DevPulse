package backend.academy.scrapper.contracts;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ContractConsistencyTest {

    private static final Path SCRAPPER_OPENAPI = Path.of("contracts/openapi.yaml");
    private static final Path SCRAPPER_ASYNCAPI = Path.of("contracts/asyncapi.yaml");

    @Test
    void openapi_shouldDescribeInternalAuthHeaderAnd401ForProtectedEndpoints() throws IOException {
        String openapi = Files.readString(SCRAPPER_OPENAPI);

        assertTrue(openapi.contains("InternalSecretHeader"));
        assertTrue(openapi.contains("name: X-Internal-Secret"));
        assertTrue(openapi.contains("'401':"));
        assertTrue(openapi.contains("Unauthorized"));
    }

    @Test
    void asyncapi_shouldDescribeOutboxTopicAndLinkUpdatePayload() throws IOException {
        String asyncapi = Files.readString(SCRAPPER_ASYNCAPI);

        assertTrue(asyncapi.contains("link-updates"));
        assertTrue(asyncapi.contains("BotLinkUpdateMessage"));
        assertTrue(asyncapi.contains("sendBotLinkUpdate"));
        assertTrue(asyncapi.contains("LinkUpdate"));
        assertTrue(asyncapi.contains("clientsIds"));
    }
}

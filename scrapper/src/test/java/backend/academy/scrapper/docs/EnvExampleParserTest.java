package backend.academy.scrapper.docs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EnvExampleParserTest {

    @Test
    void extractKeys_shouldReturnOnlyEnvKeysAndKeepOrder() {
        String content =
                """
            # comment
            BOT_POSTGRES_USER=postgres

            INVALID_LINE_WITHOUT_EQUALS
            SCRAPPER_DELIVERY_MODE=http
            BOT_POSTGRES_USER=postgres
            SO_ACCESS_TOKEN=
            """;

        Set<String> keys = EnvExampleParser.extractKeys(content);

        assertEquals(3, keys.size());
        assertIterableEquals(List.of("BOT_POSTGRES_USER", "SCRAPPER_DELIVERY_MODE", "SO_ACCESS_TOKEN"), keys);
    }
}

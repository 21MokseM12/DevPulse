package backend.academy.bot.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;

class LinkSettingsParserTest {

    @Test
    void parseSettings_splitsBySpaceAndRemovesDuplicates() {
        Set<String> result = LinkSettingsParser.parseSettings("java spring java");

        assertEquals(Set.of("java", "spring"), result);
    }
}

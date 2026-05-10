package backend.academy.scrapper.docs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class DefenseChecklistParserTest {

    @Test
    void parseChecklistItems_shouldReadCheckedAndUncheckedRows() {
        String markdown =
                """
            - [x] Выполнить `./mvnw clean verify`
            - [ ] Запустить smoke сценарий
            - [X] Обновить `docs/quality/defense-acceptance-checklist.md`
            """;

        List<DefenseChecklistParser.ChecklistItem> items = DefenseChecklistParser.parseChecklistItems(markdown);

        assertEquals(3, items.size());
        assertTrue(items.get(0).checked());
        assertFalse(items.get(1).checked());
        assertTrue(items.get(2).checked());
    }

    @Test
    void extractBacktickPaths_shouldReturnOnlyPathLikeTokens() {
        String text = "Проверить `docs/quality/backend-test-matrix.md`, "
                + "`./mvnw clean verify` и `docs/runbooks/README.md`";

        List<String> paths = DefenseChecklistParser.extractBacktickPaths(text);

        assertIterableEquals(List.of("docs/quality/backend-test-matrix.md", "docs/runbooks/README.md"), paths);
    }
}

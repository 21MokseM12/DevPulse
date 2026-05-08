package backend.academy.scrapper.docs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class TestMatrixParserTest {

    @Test
    void parseRows_shouldExtractOnlyValidMatrixRows() {
        String markdown =
                """
            | Requirement | ... | Status |
            | --- | --- | --- |
            | F1 | scope | contract | `a/A.java`<br>`b/B.java` | green |
            | X1 | scope | contract | `x/X.java` | green |
            | F2 | scope | contract | backend-only | gap |
            """;

        List<TestMatrixParser.TestMatrixRow> rows = TestMatrixParser.parseRows(markdown);

        assertEquals(2, rows.size());
        assertEquals("F1", rows.getFirst().requirement());
        assertEquals("green", rows.getFirst().status());
        assertEquals("F2", rows.get(1).requirement());
        assertEquals("gap", rows.get(1).status());
    }

    @Test
    void extractEvidencePaths_shouldReturnOnlyJavaPathsInBackticks() {
        String evidence = "`bot/src/test/java/a/A.java`<br>`README.md`<br>`scrapper/src/test/java/b/B.java`";

        List<String> paths = TestMatrixParser.extractEvidencePaths(evidence);

        assertIterableEquals(List.of("bot/src/test/java/a/A.java", "scrapper/src/test/java/b/B.java"), paths);
    }
}

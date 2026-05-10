package backend.academy.scrapper.docs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class TraceabilityMatrixParserTest {

    @Test
    void parseRows_shouldReturnValidTraceabilityRows() {
        String markdown =
                """
            | Requirement | Implementation | Test evidence | Status | Residual risk |
            | --- | --- | --- | --- | --- |
            | F1 | `a/A.java` | `a/ATest.java` | implemented,tested | none |
            | F2 | `b/B.java` | none | implemented,residual-risk | external |
            | X3 | `x/X.java` | `x/XTest.java` | implemented,tested | none |
            """;

        List<TraceabilityMatrixParser.TraceabilityRow> rows = TraceabilityMatrixParser.parseRows(markdown);

        assertEquals(2, rows.size());
        assertEquals("F1", rows.getFirst().requirement());
        assertEquals("implemented,tested", rows.getFirst().status());
        assertEquals("F2", rows.get(1).requirement());
        assertEquals("implemented,residual-risk", rows.get(1).status());
    }

    @Test
    void extractBacktickPaths_shouldReturnAllBacktickedPaths() {
        String content = "`a/A.java`<br>`b/B.java`";

        List<String> paths = TraceabilityMatrixParser.extractBacktickPaths(content);

        assertIterableEquals(List.of("a/A.java", "b/B.java"), paths);
    }
}

package backend.academy.scrapper.docs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BackendTestMatrixIntegrationTest {

    private static final Path ROOT = Path.of("..");
    private static final Path MATRIX_PATH = ROOT.resolve("docs/quality/backend-test-matrix.md");

    @Test
    void matrix_shouldCoverAllFRequirementsWithValidStatusesAndEvidenceLinks() throws IOException {
        assertTrue(Files.isRegularFile(MATRIX_PATH), "Missing matrix file docs/quality/backend-test-matrix.md");
        String markdown = Files.readString(MATRIX_PATH, StandardCharsets.UTF_8);
        List<TestMatrixParser.TestMatrixRow> rows = TestMatrixParser.parseRows(markdown);

        assertEquals(15, rows.size(), "Matrix must include exactly F1..F15 rows");

        Set<String> requirements = new LinkedHashSet<>();
        boolean hasGap = false;
        for (TestMatrixParser.TestMatrixRow row : rows) {
            requirements.add(row.requirement());
            if ("gap".equals(row.status())) {
                hasGap = true;
            }

            List<String> evidencePaths = TestMatrixParser.extractEvidencePaths(row.testEvidence());
            if ("green".equals(row.status())) {
                assertTrue(
                        !evidencePaths.isEmpty(), () -> "Green row must contain test evidence: " + row.requirement());
            }

            for (String relativePath : evidencePaths) {
                assertTrue(
                        Files.isRegularFile(ROOT.resolve(relativePath)),
                        () -> "Missing evidence file: " + relativePath);
            }
        }

        Set<String> expected =
                Set.of("F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12", "F13", "F14", "F15");
        assertEquals(expected, requirements);
        assertTrue(hasGap, "Matrix should explicitly track at least one open gap when backend coverage is partial");
    }

    @Test
    void matrix_shouldContainCiConsistencyCheckSection() throws IOException {
        String markdown = Files.readString(MATRIX_PATH, StandardCharsets.UTF_8);

        assertTrue(markdown.contains("## CI consistency check"));
        assertTrue(markdown.contains("BackendTestMatrixIntegrationTest"));
    }
}

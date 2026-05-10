package backend.academy.scrapper.docs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TraceabilityMatrixIntegrationTest {

    private static final Path ROOT = Path.of("..");
    private static final Path TRACEABILITY_PATH = ROOT.resolve("docs/quality/traceability-matrix.md");
    private static final Path TEST_MATRIX_PATH = ROOT.resolve("docs/quality/backend-test-matrix.md");
    private static final Path CHECKLIST_PATH = ROOT.resolve("docs/quality/defense-acceptance-checklist.md");

    @Test
    void traceabilityMatrix_shouldCoverF1ToF15WithValidLinksAndStatuses() throws IOException {
        String traceabilityMarkdown = Files.readString(TRACEABILITY_PATH, StandardCharsets.UTF_8);
        List<TraceabilityMatrixParser.TraceabilityRow> rows = TraceabilityMatrixParser.parseRows(traceabilityMarkdown);

        assertEquals(15, rows.size(), "Traceability matrix must include F1..F15");

        Set<String> requirements = new LinkedHashSet<>();
        Map<String, TraceabilityMatrixParser.TraceabilityRow> byRequirement = new LinkedHashMap<>();
        for (TraceabilityMatrixParser.TraceabilityRow row : rows) {
            requirements.add(row.requirement());
            byRequirement.put(row.requirement(), row);

            List<String> implementationPaths = TraceabilityMatrixParser.extractBacktickPaths(row.implementation());
            if ("implemented,tested".equals(row.status())) {
                assertTrue(
                        !implementationPaths.isEmpty(), () -> "Implementation links missing for " + row.requirement());
            }

            for (String relativePath : implementationPaths) {
                assertTrue(
                        pathExistsAllowingWildcard(relativePath),
                        () -> "Implementation path does not exist: " + relativePath);
            }

            List<String> testPaths = TraceabilityMatrixParser.extractBacktickPaths(row.testEvidence());
            if ("implemented,tested".equals(row.status())) {
                assertTrue(!testPaths.isEmpty(), () -> "Test evidence missing for " + row.requirement());
            }
            for (String relativePath : testPaths) {
                assertTrue(
                        pathExistsAllowingWildcard(relativePath),
                        () -> "Test evidence path does not exist: " + relativePath);
            }
        }

        Set<String> expected =
                Set.of("F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12", "F13", "F14", "F15");
        assertEquals(expected, requirements);
        assertEquals("implemented,residual-risk", byRequirement.get("F12").status());
    }

    @Test
    void traceability_shouldBeConsistentWithBackendTestMatrixAndChecklist() throws IOException {
        String traceabilityMarkdown = Files.readString(TRACEABILITY_PATH, StandardCharsets.UTF_8);
        String testMatrixMarkdown = Files.readString(TEST_MATRIX_PATH, StandardCharsets.UTF_8);
        String checklistMarkdown = Files.readString(CHECKLIST_PATH, StandardCharsets.UTF_8);

        List<TraceabilityMatrixParser.TraceabilityRow> traceRows =
                TraceabilityMatrixParser.parseRows(traceabilityMarkdown);
        List<TestMatrixParser.TestMatrixRow> testRows = TestMatrixParser.parseRows(testMatrixMarkdown);

        Map<String, String> traceStatuses = new LinkedHashMap<>();
        for (TraceabilityMatrixParser.TraceabilityRow row : traceRows) {
            traceStatuses.put(row.requirement(), row.status());
        }

        for (TestMatrixParser.TestMatrixRow row : testRows) {
            String traceStatus = traceStatuses.get(row.requirement());
            assertTrue(traceStatus != null, () -> "Missing traceability row for " + row.requirement());

            if ("green".equals(row.status())) {
                assertEquals(
                        "implemented,tested", traceStatus, "Green requirement must be tested in traceability matrix");
            } else if ("gap".equals(row.status())) {
                assertEquals(
                        "implemented,residual-risk",
                        traceStatus,
                        "Gap requirement must be residual-risk in traceability matrix");
            }
        }

        assertTrue(checklistMarkdown.contains("docs/quality/backend-test-matrix.md"));
        assertTrue(checklistMarkdown.contains("docs/quality/traceability-matrix.md"));
        assertTrue(checklistMarkdown.contains("./mvnw clean verify"));
    }

    private static boolean pathExistsAllowingWildcard(String relativePath) {
        if (relativePath.contains("*")) {
            String prefix = relativePath.substring(0, relativePath.indexOf('*'));
            return Files.exists(ROOT.resolve(prefix));
        }
        return Files.exists(ROOT.resolve(relativePath));
    }
}

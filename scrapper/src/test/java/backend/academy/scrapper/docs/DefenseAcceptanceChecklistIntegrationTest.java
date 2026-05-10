package backend.academy.scrapper.docs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class DefenseAcceptanceChecklistIntegrationTest {

    private static final Path ROOT = Path.of("..");
    private static final Path CHECKLIST_PATH = ROOT.resolve("docs/quality/defense-acceptance-checklist.md");

    @Test
    void checklist_shouldContainBk504EvidenceWithSuccessfulVerifyMark() throws IOException {
        assertTrue(
                Files.isRegularFile(CHECKLIST_PATH), "Missing checklist docs/quality/defense-acceptance-checklist.md");
        String markdown = Files.readString(CHECKLIST_PATH, StandardCharsets.UTF_8);

        assertTrue(markdown.contains("## 7) BK-504 verification evidence"));
        assertTrue(markdown.contains("Verification result: PASS"));

        List<DefenseChecklistParser.ChecklistItem> items = DefenseChecklistParser.parseChecklistItems(markdown);
        assertTrue(
                items.stream()
                        .anyMatch(item -> item.checked() && item.text().contains("Выполнить `./mvnw clean verify`")),
                "Checklist must include checked verify item");

        for (DefenseChecklistParser.ChecklistItem item : items) {
            for (String relativePath : DefenseChecklistParser.extractBacktickPaths(item.text())) {
                assertTrue(Files.exists(ROOT.resolve(relativePath)), () -> "Missing referenced path: " + relativePath);
            }
        }
    }
}

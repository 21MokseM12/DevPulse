package backend.academy.scrapper.docs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class RunbooksPackageIntegrationTest {

    private static final Path RUNBOOKS_ROOT = Path.of("..", "docs", "runbooks");

    @Test
    void runbooks_shouldContainOperationalPackageWithRequiredFilesAndChecklist() throws IOException {
        List<Path> runbooks = List.of(
                RUNBOOKS_ROOT.resolve("backup-restore-postgres.md"),
                RUNBOOKS_ROOT.resolve("rollback-cd-k8s.md"),
                RUNBOOKS_ROOT.resolve("post-deploy-smoke.md"),
                RUNBOOKS_ROOT.resolve("incident-quick-guide.md"));

        for (Path runbook : runbooks) {
            assertTrue(Files.isRegularFile(runbook), () -> "Missing runbook file: " + runbook.getFileName());
            String content = Files.readString(runbook, StandardCharsets.UTF_8);
            assertTrue(
                    RunbookChecklistValidator.hasRequiredProcedureSections(content),
                    () -> "Runbook checklist sections are incomplete: " + runbook.getFileName());
            assertTrue(content.contains("## Секреты и безопасная работа"), () -> "Missing secrets section: " + runbook);
        }
    }

    @Test
    void runbooksReadme_shouldReferenceCdPipelinesAndKubernetesManifests() throws IOException {
        Path readmePath = RUNBOOKS_ROOT.resolve("README.md");
        String readme = Files.readString(readmePath, StandardCharsets.UTF_8);

        assertTrue(readme.contains(".github/workflows/cd-staging.yaml"));
        assertTrue(readme.contains(".github/workflows/cd-production.yaml"));
        assertTrue(readme.contains("k8s/staging/*"));
        assertTrue(readme.contains("k8s/production/*"));
        assertTrue(readme.contains("Tabletop-проверка"));
    }
}

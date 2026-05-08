package backend.academy.scrapper.ci;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class GithubWorkflowStructureTest {

    private static final Path WORKFLOWS_ROOT = Path.of("..", ".github", "workflows");

    @Test
    void workflows_shouldContainCiAndCdPipelines() {
        List<String> expected = List.of("build.yaml", "cd-staging.yaml", "cd-production.yaml");
        for (String workflow : expected) {
            assertTrue(
                    Files.isRegularFile(WORKFLOWS_ROOT.resolve(workflow)), () -> "Missing workflow file: " + workflow);
        }
    }
}

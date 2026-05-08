package backend.academy.scrapper.ci;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GithubCdWorkflowIntegrationTest {

    @Test
    void workflows_shouldContainHealthChecksRollbacksAndArtifacts() throws IOException {
        String ci = read(Path.of("..", ".github", "workflows", "build.yaml"));
        String stagingCd = read(Path.of("..", ".github", "workflows", "cd-staging.yaml"));
        String productionCd = read(Path.of("..", ".github", "workflows", "cd-production.yaml"));

        assertTrue(ci.contains("name: CI"));
        assertTrue(ci.contains("build-and-push-images"));
        assertTrue(ci.contains("docker/build-push-action"));
        assertTrue(ci.contains("ghcr.io/${{ github.repository_owner }}/devpulse-bot"));

        assertTrue(stagingCd.contains("name: CD Staging"));
        assertTrue(stagingCd.contains("workflow_run"));
        assertTrue(stagingCd.contains("Post-deploy health check"));
        assertTrue(stagingCd.contains("Rollout on failure") || stagingCd.contains("Rollback on failure"));
        assertTrue(stagingCd.contains("rollout undo deployment/bot"));
        assertTrue(stagingCd.contains("upload-artifact"));

        assertTrue(productionCd.contains("name: CD Production"));
        assertTrue(productionCd.contains("environment: production"));
        assertTrue(productionCd.contains("Post-deploy health check"));
        assertTrue(productionCd.contains("rollout undo deployment/scrapper"));
        assertTrue(productionCd.contains("upload-artifact"));
    }

    private static String read(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}

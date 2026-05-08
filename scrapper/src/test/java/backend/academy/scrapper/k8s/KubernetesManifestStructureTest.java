package backend.academy.scrapper.k8s;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class KubernetesManifestStructureTest {

    private static final Path K8S_ROOT = Path.of("..", "k8s");

    @Test
    void stagingAndProduction_shouldContainExpectedManifestSet() {
        List<String> expectedFiles = List.of(
                "namespace.yaml",
                "configmap.yaml",
                "secret.example.yaml",
                "stateful-services.yaml",
                "kafka-topics-job.yaml",
                "bot.yaml",
                "scrapper.yaml",
                "README.md");

        assertEnvironmentFiles("staging", expectedFiles);
        assertEnvironmentFiles("production", expectedFiles);
    }

    private static void assertEnvironmentFiles(String environment, List<String> expectedFiles) {
        Path environmentPath = K8S_ROOT.resolve(environment);
        assertTrue(Files.isDirectory(environmentPath), () -> "Missing k8s directory: " + environmentPath);
        for (String file : expectedFiles) {
            assertTrue(Files.isRegularFile(environmentPath.resolve(file)), () -> "Missing manifest file: " + file);
        }
    }
}

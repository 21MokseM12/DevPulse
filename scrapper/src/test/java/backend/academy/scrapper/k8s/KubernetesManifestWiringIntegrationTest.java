package backend.academy.scrapper.k8s;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class KubernetesManifestWiringIntegrationTest {

    @Test
    void manifests_shouldContainProbesHpaAndTopicBootstrapForEachEnvironment() throws IOException {
        assertEnvironmentWiring("staging", "devpulse-staging");
        assertEnvironmentWiring("production", "devpulse-production");
    }

    private static void assertEnvironmentWiring(String environment, String namespace) throws IOException {
        Path root = Path.of("..", "k8s", environment);
        String botManifest = read(root.resolve("bot.yaml"));
        String scrapperManifest = read(root.resolve("scrapper.yaml"));
        String statefulManifest = read(root.resolve("stateful-services.yaml"));
        String kafkaJobManifest = read(root.resolve("kafka-topics-job.yaml"));

        assertTrue(botManifest.contains("kind: HorizontalPodAutoscaler"));
        assertTrue(scrapperManifest.contains("kind: HorizontalPodAutoscaler"));
        assertTrue(botManifest.contains("readinessProbe"));
        assertTrue(botManifest.contains("livenessProbe"));
        assertTrue(scrapperManifest.contains("readinessProbe"));
        assertTrue(scrapperManifest.contains("livenessProbe"));
        assertTrue(botManifest.contains("maxUnavailable: 0"));
        assertTrue(scrapperManifest.contains("maxUnavailable: 0"));
        assertTrue(statefulManifest.contains("name: zookeeper"));
        assertTrue(statefulManifest.contains("name: kafka"));
        assertTrue(kafkaJobManifest.contains("topic link-updates"));
        assertTrue(kafkaJobManifest.contains("topic link-topic-response"));
        assertTrue(botManifest.contains("namespace: " + namespace));
        assertTrue(scrapperManifest.contains("namespace: " + namespace));
    }

    private static String read(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}

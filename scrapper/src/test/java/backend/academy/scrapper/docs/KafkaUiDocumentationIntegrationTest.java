package backend.academy.scrapper.docs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class KafkaUiDocumentationIntegrationTest {

    private static final Path ROOT = Path.of("..");
    private static final Path COMPOSE_PATH = ROOT.resolve("docker-compose.yaml");
    private static final Path README_PATH = ROOT.resolve("README.md");

    @Test
    void compose_shouldExposeKafkaUiServiceAndBootstrapConnection() throws IOException {
        String compose = read(COMPOSE_PATH);

        assertTrue(KafkaUiComposeValidator.hasKafkaUiService(compose));
        assertTrue(KafkaUiComposeValidator.hasKafkaUiKafkaConnection(compose));
        assertTrue(compose.contains("KAFKA_CLUSTERS_0_ZOOKEEPER=zookeeper:2181"));
    }

    @Test
    void readme_shouldContainKafkaUiLinkForLocalSmoke() throws IOException {
        String readme = read(README_PATH);

        assertTrue(readme.contains("http://localhost:8082"));
        assertTrue(readme.contains("Открыть Kafka UI"));
    }

    private static String read(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}

package backend.academy.scrapper.integration_test.kafka;

import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.scrapper.integration_test.config.KafkaTopics;
import backend.academy.scrapper.integration_test.config.TestContainersConfiguration;
import java.util.Properties;
import java.util.Set;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.junit.jupiter.api.Test;

class KafkaTopicsBootstrapIntegrationTest extends TestContainersConfiguration {

    @Test
    void bootstrap_createsAllRequiredTopics() {
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        try (AdminClient adminClient = AdminClient.create(properties)) {
            Set<String> existingTopics = adminClient.listTopics().names().get();
            assertTrue(existingTopics.containsAll(KafkaTopics.REQUIRED_TOPICS));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

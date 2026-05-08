package backend.academy.scrapper.integration_test.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import org.junit.jupiter.api.Test;

class KafkaTopicsTest {

    @Test
    void requiredTopics_containsExpectedNamesWithoutDuplicates() {
        assertEquals(4, KafkaTopics.REQUIRED_TOPICS.size());
        assertEquals(KafkaTopics.REQUIRED_TOPICS.size(), new HashSet<>(KafkaTopics.REQUIRED_TOPICS).size());
        assertTrue(KafkaTopics.REQUIRED_TOPICS.contains("link-updates"));
        assertTrue(KafkaTopics.REQUIRED_TOPICS.contains("client-listener-topic-request"));
        assertTrue(KafkaTopics.REQUIRED_TOPICS.contains("link-listener-topic-request"));
        assertTrue(KafkaTopics.REQUIRED_TOPICS.contains("link-topic-response"));
    }
}

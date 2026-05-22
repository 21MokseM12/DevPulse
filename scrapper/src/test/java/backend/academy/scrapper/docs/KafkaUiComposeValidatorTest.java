package backend.academy.scrapper.docs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class KafkaUiComposeValidatorTest {

    @Test
    void hasKafkaUiService_shouldReturnTrueWhenComposeContainsUiServiceAndPort() {
        String compose =
                """
            services:
              kafka-ui:
                image: provectuslabs/kafka-ui:latest
                ports:
                  - "8082:8080"
            """;

        assertTrue(KafkaUiComposeValidator.hasKafkaUiService(compose));
    }

    @Test
    void hasKafkaUiKafkaConnection_shouldReturnFalseWhenBootstrapServerMissing() {
        String compose =
                """
            services:
              kafka-ui:
                environment:
                  - KAFKA_CLUSTERS_0_NAME=local
            """;

        assertFalse(KafkaUiComposeValidator.hasKafkaUiKafkaConnection(compose));
    }
}

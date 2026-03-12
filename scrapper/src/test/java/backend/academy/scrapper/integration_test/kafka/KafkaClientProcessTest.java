package backend.academy.scrapper.integration_test.kafka;

import backend.academy.scrapper.config.CommonKafkaConfig;
import backend.academy.scrapper.db.repository.ChatRepository;
import backend.academy.scrapper.enums.actions.ClientActions;
import backend.academy.scrapper.integration_test.TestApplication;
import backend.academy.scrapper.model.kafka.ClientMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class KafkaClientProcessTest extends TestApplication {

    @Autowired
    @Qualifier(CommonKafkaConfig.COMMON_KAFKA_TEMPLATE)
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ChatRepository chatRepository;

    @Value("${kafka.consumers.client-listener.topic}")
    private String clientListenerTopic;

    @Test
    void whenRegisterMessageSent_thenClientRegisteredInDatabase() {
        Long id = 4L;
        assertFalse(chatRepository.isClient(id), "Chat should not exist before test");

        ClientMessage message = new ClientMessage(ClientActions.REGISTER, id);
        kafkaTemplate.send(clientListenerTopic, String.valueOf(id), message);

        await().until(() -> chatRepository.isClient(id));

        assertTrue(chatRepository.isClient(id));
    }

    @Test
    void whenUnregisterMessageSent_thenClientRemovedFromDatabase() {
        Long id = 5L;
        chatRepository.save(id);
        assertTrue(chatRepository.isClient(id), "Chat should exist before unregister");

        ClientMessage message = new ClientMessage(ClientActions.UNREGISTER, id);
        kafkaTemplate.send(clientListenerTopic, String.valueOf(id), message);

        await().untilAsserted(() -> assertFalse(chatRepository.isClient(id)));

        assertFalse(chatRepository.isClient(id));
    }

    @Test
    void whenRegisterAndUnregisterMessageSent_thenFullLifecycleProcessed() {
        Long id = 9948L;
        assertFalse(chatRepository.isClient(id), "Chat should not exist before test");

        kafkaTemplate.send(clientListenerTopic, "key", new ClientMessage(ClientActions.REGISTER, id));
        await().until(() -> chatRepository.isClient(id));

        kafkaTemplate.send(clientListenerTopic, "key", new ClientMessage(ClientActions.UNREGISTER, id));
        await().untilAsserted(() -> assertFalse(chatRepository.isClient(id)));

        assertFalse(chatRepository.isClient(id));
    }
}

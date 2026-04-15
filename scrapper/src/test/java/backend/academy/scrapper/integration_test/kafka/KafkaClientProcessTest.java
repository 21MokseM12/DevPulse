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
        String login = "kafka_client_register";
        String password = "kafka_pass_1";
        assertFalse(chatRepository.isClient(login, password), "Chat should not exist before test");

        ClientMessage message = new ClientMessage(ClientActions.REGISTER, login, password);
        kafkaTemplate.send(clientListenerTopic, login, message);

        await().until(() -> chatRepository.isClient(login, password));

        assertTrue(chatRepository.isClient(login, password));
    }

    @Test
    void whenUnregisterMessageSent_thenClientRemovedFromDatabase() {
        String login = "kafka_client_unregister";
        String password = "kafka_pass_2";
        chatRepository.save(login, password);
        assertTrue(chatRepository.isClient(login, password), "Chat should exist before unregister");

        ClientMessage message = new ClientMessage(ClientActions.UNREGISTER, login, password);
        kafkaTemplate.send(clientListenerTopic, login, message);

        await().untilAsserted(() -> assertFalse(chatRepository.isClient(login, password)));

        assertFalse(chatRepository.isClient(login, password));
    }

    @Test
    void whenRegisterAndUnregisterMessageSent_thenFullLifecycleProcessed() {
        String login = "kafka_client_lifecycle";
        String password = "kafka_pass_3";
        assertFalse(chatRepository.isClient(login, password), "Chat should not exist before test");

        kafkaTemplate.send(clientListenerTopic, "key", new ClientMessage(ClientActions.REGISTER, login, password));
        await().until(() -> chatRepository.isClient(login, password));

        kafkaTemplate.send(clientListenerTopic, "key", new ClientMessage(ClientActions.UNREGISTER, login, password));
        await().untilAsserted(() -> assertFalse(chatRepository.isClient(login, password)));

        assertFalse(chatRepository.isClient(login, password));
    }
}

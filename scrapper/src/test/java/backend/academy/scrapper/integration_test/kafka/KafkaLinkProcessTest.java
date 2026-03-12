package backend.academy.scrapper.integration_test.kafka;

import backend.academy.scrapper.config.CommonKafkaConfig;
import backend.academy.scrapper.db.repository.LinkToChatRepository;
import backend.academy.scrapper.enums.actions.LinkActions;
import backend.academy.scrapper.model.kafka.LinkMessage;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@ActiveProfiles("test")
@Sql("classpath:test-init-links.sql")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(LinkResponseCapture.class)
public class KafkaLinkProcessTest extends backend.academy.scrapper.integration_test.TestApplication {

    @Autowired
    @Qualifier(CommonKafkaConfig.COMMON_KAFKA_TEMPLATE)
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private LinkResponseCapture responseCapture;

    @Autowired
    private LinkToChatRepository linkToChatRepository;

    @Value("${kafka.consumers.link-listener.topic}")
    private String linkListenerTopic;

    @BeforeEach
    void setUp() {
        responseCapture.clear();
    }

    @Test
    void whenFindAllMessageSent_thenResponseReceived() {
        Long chatId = 100L;
        LinkMessage message = new LinkMessage(
            LinkActions.FIND_ALL,
            chatId,
            null,
            null
        );

        kafkaTemplate.send(linkListenerTopic, String.valueOf(chatId), message);

        List<LinkResponse> response = await()
            .atMost(10, TimeUnit.SECONDS)
            .until(() -> responseCapture.pollResponse(1, TimeUnit.SECONDS), r -> r != null && !r.isEmpty());

        assertNotNull(response);
        assertEquals(2, response.size());
    }

    @Test
    void whenSubscribeMessageSent_thenResponseReceived() {
        Long chatId = 100L;
        URI linkUri = URI.create("https://github.com/owner/repo3");
        AddLinkRequest addRequest = new AddLinkRequest(linkUri, Set.of("tag"), Set.of("filter"));
        LinkMessage message = new LinkMessage(
            LinkActions.SUBSCRIBE,
            chatId,
            addRequest,
            null
        );

        kafkaTemplate.send(linkListenerTopic, String.valueOf(chatId), message);

        List<LinkResponse> response = await()
            .atMost(10, TimeUnit.SECONDS)
            .until(() -> responseCapture.pollResponse(1, TimeUnit.SECONDS), r -> r != null && !r.isEmpty());

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(linkUri, response.getFirst().url());
        assertTrue(linkToChatRepository.chatIsSubscribedOnLink(chatId, response.getFirst().id()));
    }

    @Test
    void whenUnsubscribeMessageSent_thenResponseReceived() throws InterruptedException {
        Long chatId = 100L;
        URI linkUri = URI.create("https://github.com/owner/repo1");
        RemoveLinkRequest removeRequest = new RemoveLinkRequest(linkUri);
        LinkMessage message = new LinkMessage(
            LinkActions.UNSUBSCRIBE,
            chatId,
            null,
            removeRequest
        );

        assertTrue(linkToChatRepository.chatIsSubscribedOnLink(chatId, 1L), "Chat should be subscribed before test");

        kafkaTemplate.send(linkListenerTopic, String.valueOf(chatId), message);

        List<LinkResponse> response = await()
            .atMost(10, TimeUnit.SECONDS)
            .until(() -> responseCapture.pollResponse(1, TimeUnit.SECONDS), r -> r != null && !r.isEmpty());

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(linkUri, response.getFirst().url());
        await().atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> assertFalse(linkToChatRepository.chatIsSubscribedOnLink(chatId, 1L)));
    }
}

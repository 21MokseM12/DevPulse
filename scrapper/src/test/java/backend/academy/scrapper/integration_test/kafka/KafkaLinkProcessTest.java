package backend.academy.scrapper.integration_test.kafka;

import backend.academy.scrapper.config.CommonKafkaConfig;
import backend.academy.scrapper.db.repository.LinkToChatRepository;
import backend.academy.scrapper.enums.actions.LinkActions;
import backend.academy.scrapper.integration_test.TestApplication;
import backend.academy.scrapper.model.kafka.LinkMessage;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
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
@Sql(scripts = "classpath:test-init-links.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(LinkResponseCapture.class)
public class KafkaLinkProcessTest extends TestApplication {

    @Autowired
    @Qualifier(CommonKafkaConfig.COMMON_KAFKA_TEMPLATE)
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private LinkResponseCapture responseCapture;
    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private LinkToChatRepository linkToChatRepository;

    @Value("${kafka.consumers.link-listener.topic}")
    private String linkListenerTopic;
    private static final String CLIENT_LOGIN = "test-client-100";
    private static final String CLIENT_PASSWORD = "test-password-100";

    @BeforeEach
    void setUp() {
        // Accessing registry here ensures listener containers are initialized before polling responses.
        kafkaListenerEndpointRegistry.getListenerContainer(LinkResponseCapture.LISTENER_ID);
        responseCapture.clear();
    }

    @Test
    void whenFindAllMessageSent_thenResponseReceived() {
        Long chatId = 100L;
        Set<URI> expectedUrls = Set.of(
            URI.create("https://github.com/owner/repo1"),
            URI.create("https://github.com/owner/repo2")
        );
        LinkMessage message = new LinkMessage(
            LinkActions.FIND_ALL,
            CLIENT_LOGIN,
            CLIENT_PASSWORD,
            null,
            null
        );

        kafkaTemplate.send(linkListenerTopic, String.valueOf(chatId), message);
        kafkaTemplate.flush();

        List<LinkResponse> response = awaitResponseMatching(r ->
            r.stream().map(LinkResponse::url).collect(Collectors.toSet()).containsAll(expectedUrls)
        );

        assertNotNull(response);
        Set<URI> actualUrls = response.stream().map(LinkResponse::url).collect(Collectors.toSet());
        assertTrue(actualUrls.containsAll(expectedUrls));
    }

    @Test
    void whenSubscribeMessageSent_thenResponseReceived() {
        Long chatId = 100L;
        URI linkUri = URI.create("https://github.com/owner/repo3");
        AddLinkRequest addRequest = new AddLinkRequest(linkUri, Set.of("tag"), Set.of("filter"));
        LinkMessage message = new LinkMessage(
            LinkActions.SUBSCRIBE,
            CLIENT_LOGIN,
            CLIENT_PASSWORD,
            addRequest,
            null
        );

        kafkaTemplate.send(linkListenerTopic, String.valueOf(chatId), message);
        kafkaTemplate.flush();

        List<LinkResponse> response = awaitResponseMatching(
            r -> r.size() == 1 && linkUri.equals(r.getFirst().url())
        );

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
            CLIENT_LOGIN,
            CLIENT_PASSWORD,
            null,
            removeRequest
        );

        assertTrue(linkToChatRepository.chatIsSubscribedOnLink(chatId, 1L), "Chat should be subscribed before test");

        kafkaTemplate.send(linkListenerTopic, String.valueOf(chatId), message);
        kafkaTemplate.flush();

        List<LinkResponse> response = awaitResponseMatching(
            r -> r.size() == 1 && linkUri.equals(r.getFirst().url())
        );

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(linkUri, response.getFirst().url());
    }

    private List<LinkResponse> awaitResponseMatching(Predicate<List<LinkResponse>> matcher) {
        return await()
            .atMost(30, TimeUnit.SECONDS)
            .until(
                () -> responseCapture.pollResponse(1, TimeUnit.SECONDS),
                response -> response != null && !response.isEmpty() && matcher.test(response)
            );
    }
}

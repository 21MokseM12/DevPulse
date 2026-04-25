package backend.academy.scrapper.integration_test.service;

import backend.academy.scrapper.ScrapperApplication;
import backend.academy.scrapper.integration_test.config.TestContainersConfiguration;
import backend.academy.scrapper.service.ChatOperationProcessor;
import backend.academy.scrapper.service.LinkOperationProcessor;
import java.net.URI;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {ScrapperApplication.class})
@ActiveProfiles("test")
class LinkOperationProcessorTest extends TestContainersConfiguration {

    @Autowired
    private ChatOperationProcessor chatOperationProcessor;

    @Autowired
    private LinkOperationProcessor linkOperationProcessor;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void subscribe_whenAlreadySubscribed_returnsExistingLinkWithoutDuplicateRelation() {
        Long chatId = 50101L;
        URI url = URI.create("https://link-operation-it.example/subscription-idempotent");
        AddLinkRequest request = new AddLinkRequest(url, Set.of("tag"), Set.of("filter"));

        chatOperationProcessor.register(chatId);

        LinkResponse firstResponse = linkOperationProcessor.subscribe(chatId, request).orElseThrow();
        LinkResponse secondResponse = linkOperationProcessor.subscribe(chatId, request).orElseThrow();

        assertEquals(firstResponse.id(), secondResponse.id());
        assertEquals(firstResponse.url(), secondResponse.url());

        Integer relationCount = jdbcTemplate.queryForObject(
            "select count(*) from client_links where client_id = ? and link_id = ?",
            Integer.class,
            chatId,
            firstResponse.id()
        );
        Integer linksCount = jdbcTemplate.queryForObject(
            "select count(*) from links where url = ?",
            Integer.class,
            url.toString()
        );

        assertEquals(1, relationCount);
        assertEquals(1, linksCount);
        assertTrue(chatOperationProcessor.chatIsSubscribedOnLink(chatId, firstResponse.id()));
    }
}

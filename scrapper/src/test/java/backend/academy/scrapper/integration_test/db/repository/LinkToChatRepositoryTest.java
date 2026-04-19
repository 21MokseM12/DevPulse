package backend.academy.scrapper.integration_test.db.repository;

import backend.academy.scrapper.db.repository.LinkToChatRepository;
import backend.academy.scrapper.db.repository.impl.LinkToChatRepositoryImpl;
import backend.academy.scrapper.integration_test.config.TestContainersConfiguration;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.dao.DuplicateKeyException;

@JdbcTest
@Testcontainers
@ActiveProfiles("test")
@Import(LinkToChatRepositoryImpl.class)
public class LinkToChatRepositoryTest extends TestContainersConfiguration {

    @Autowired
    private LinkToChatRepository repository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void subscribeChatOnLink_whenPairIsValid_thenCreatesSubscription() {
        Long chatId = createChat(20001L);
        Long linkId = createLink("https://link-to-chat.example/1");

        boolean created = repository.subscribeChatOnLink(chatId, linkId);

        assertTrue(created);
        assertTrue(repository.chatIsSubscribedOnLink(chatId, linkId));
    }

    @Test
    public void subscribeChatOnLink_twice_isIdempotent_singleRowInDb() {
        Long chatId = createChat(20008L);
        Long linkId = createLink("https://link-to-chat.example/idempotent");

        assertTrue(repository.subscribeChatOnLink(chatId, linkId));
        assertTrue(repository.subscribeChatOnLink(chatId, linkId));

        Integer count = jdbcTemplate.queryForObject(
            "select count(*) from links_chats where chat_id = ? and link_id = ?",
            Integer.class,
            chatId,
            linkId);
        assertEquals(1, count);
    }

    @Test
    public void linksChats_uniqueConstraint_rejectsRawDuplicateInsert() {
        Long chatId = createChat(20009L);
        Long linkId = createLink("https://link-to-chat.example/unique-raw");

        jdbcTemplate.update("insert into links_chats (chat_id, link_id) values (?, ?)", chatId, linkId);

        assertThrows(DuplicateKeyException.class, () ->
            jdbcTemplate.update("insert into links_chats (chat_id, link_id) values (?, ?)", chatId, linkId));
    }

    @Test
    public void subscribeChatOnLink_whenIdsAreNull_thenThrowsDataIntegrityViolationException() {
        assertThrows(
            DataIntegrityViolationException.class,
            () -> repository.subscribeChatOnLink(null, null)
        );
    }

    @Test
    public void chatIsSubscribedOnLink_whenIdsAreNull_thenReturnsFalse() {
        assertFalse(repository.chatIsSubscribedOnLink(null, null));
    }

    @Test
    public void unsubscribe_whenSubscriptionExists_thenReturnsTrueAndDeletes() {
        Long chatId = createChat(20002L);
        Long linkId = createLink("https://link-to-chat.example/2");
        repository.subscribeChatOnLink(chatId, linkId);

        boolean unsubscribed = repository.unsubscribe(chatId, linkId);

        assertTrue(unsubscribed);
        assertFalse(repository.chatIsSubscribedOnLink(chatId, linkId));
    }

    @Test
    public void unsubscribe_whenSubscriptionDoesNotExist_thenReturnsFalse() {
        Long chatId = createChat(20003L);
        Long linkId = createLink("https://link-to-chat.example/3");

        boolean unsubscribed = repository.unsubscribe(chatId, linkId);

        assertFalse(unsubscribed);
    }

    @Test
    public void unsubscribeAll_whenChatHasSubscriptions_thenRemovesAll() {
        Long chatId = createChat(20004L);
        Long firstLink = createLink("https://link-to-chat.example/4a");
        Long secondLink = createLink("https://link-to-chat.example/4b");
        repository.subscribeChatOnLink(chatId, firstLink);
        repository.subscribeChatOnLink(chatId, secondLink);

        repository.unsubscribeAll(chatId);

        assertFalse(repository.chatIsSubscribedOnLink(chatId, firstLink));
        assertFalse(repository.chatIsSubscribedOnLink(chatId, secondLink));
        assertTrue(repository.findAllIdByChatId(chatId).isEmpty());
    }

    @Test
    public void findAllIdByChatId_whenChatHasNoSubscriptions_thenReturnsEmptyList() {
        Long chatId = createChat(20005L);

        List<Long> links = repository.findAllIdByChatId(chatId);

        assertTrue(links.isEmpty());
    }

    @Test
    public void findAllByLinkId_whenMultipleChatsSubscribed_thenReturnsAllChatIds() {
        Long firstChat = createChat(20006L);
        Long secondChat = createChat(20007L);
        Long linkId = createLink("https://link-to-chat.example/5");
        repository.subscribeChatOnLink(firstChat, linkId);
        repository.subscribeChatOnLink(secondChat, linkId);

        List<Long> chats = repository.findAllByLinkId(linkId);

        assertEquals(new HashSet<>(List.of(firstChat, secondChat)), new HashSet<>(chats));
    }

    @Test
    public void findAllByLinkId_whenNoChatsSubscribed_thenReturnsEmptyList() {
        Long linkId = createLink("https://link-to-chat.example/6");

        List<Long> chats = repository.findAllByLinkId(linkId);

        assertTrue(chats.isEmpty());
    }

    private Long createChat(Long chatId) {
        jdbcTemplate.update("insert into chats (id) values (?)", chatId);
        return chatId;
    }

    private Long createLink(String url) {
        return jdbcTemplate.queryForObject(
            "insert into links (link, updated_at) values (?, ?) returning id",
            Long.class,
            url,
            Timestamp.from(OffsetDateTime.now().toInstant())
        );
    }
}

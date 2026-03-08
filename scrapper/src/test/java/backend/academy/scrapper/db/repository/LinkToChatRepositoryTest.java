package backend.academy.scrapper.db.repository;

import backend.academy.scrapper.db.TestContainersConfiguration;
import backend.academy.scrapper.db.repository.impl.LinkToChatRepositoryImpl;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@Testcontainers
@ActiveProfiles("test")
@Sql("classpath:test-init.sql")
@Import(LinkToChatRepositoryImpl.class)
public class LinkToChatRepositoryTest extends TestContainersConfiguration {

    @Autowired
    private LinkToChatRepository repository;

    @Test
    public void subscribeChatOnLink_whenChatIsNotSubscribed_thenReturnTrue() {
        Long chatId = 1L, linkId = 2L;
        boolean subscribed = repository.subscribeChatOnLink(chatId, linkId);
        assertTrue(subscribed);
    }

    @Test
    public void subscribeChatOnLink_whenIdsAreNull_thenReturnFalse() {
        assertThrows(DataIntegrityViolationException.class, () -> repository.subscribeChatOnLink(null, null));
    }

    @Test
    public void chatIsSubscribedOnLInk_whenChatIsSubscribed_thenReturnTrue() {
        boolean subscribed = repository.chatIsSubscribedOnLink(5L, 1L);
        assertTrue(subscribed);
    }

    @Test
    public void chatIsSubscribedOnLInk_whenChatIsNotSubscribed_thenReturnFalse() {
        boolean subscribed = repository.chatIsSubscribedOnLink(6L, 1L);
        assertFalse(subscribed);
    }

    @Test
    public void chatIsSubscribedOnLInk_whenIdsAreNull_thenReturnFalse() {
        boolean subscribed = repository.chatIsSubscribedOnLink(null, null);
        assertFalse(subscribed);
    }

    @Test
    public void unsubscribe_success() {
        repository.unsubscribe(123L, 3L);
        boolean subscribed = repository.chatIsSubscribedOnLink(123L, 3L);
        assertFalse(subscribed);
    }

    @Test
    public void unsubscribeAll_success() {
        repository.unsubscribeAll(5L);
        boolean subscribed = repository.chatIsSubscribedOnLink(5L, 1L);
        assertFalse(subscribed);
    }

    @Test
    public void findAllByChatId_whenChatIsSubscribed_thenReturnLinksIds() {
        List<Long> allIdByChatId = repository.findAllIdByChatId(6L);
        assertNotNull(allIdByChatId);
        assertFalse(allIdByChatId.isEmpty());
        assertEquals(List.of(2L), allIdByChatId);
    }

    @Test
    public void findAllByChatId_whenChatIsNotSubscribed_thenReturnEmptyList() {
        List<Long> allIdByChatId = repository.findAllIdByChatId(7L);
        assertNotNull(allIdByChatId);
        assertTrue(allIdByChatId.isEmpty());
    }

    @Test
    public void findAllByLinkId_whenLinkIsSubscribedByChats_thenReturnChatIds() {
        List<Long> expected = List.of(6L, 5L);

        List<Long> subscribedChats = repository.findAllByLinkId(2L);

        assertNotNull(subscribedChats);
        assertFalse(subscribedChats.isEmpty());
        assertEquals(expected, subscribedChats);
    }

    @Test
    public void findAllByLinkId_whenLinkIsNotSubscribedByChats_thenReturnEmptyList() {
        List<Long> subscribedChats = repository.findAllByLinkId(4L);

        assertNotNull(subscribedChats);
        assertTrue(subscribedChats.isEmpty());
    }
}

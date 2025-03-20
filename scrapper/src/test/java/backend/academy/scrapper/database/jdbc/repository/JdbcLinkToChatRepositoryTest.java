package backend.academy.scrapper.database.jdbc.repository;

import backend.academy.scrapper.database.TestContainersConfiguration;
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
@Import(JdbcLinkToChatRepository.class)
@ActiveProfiles("test")
@Sql("classpath:test-init.sql")
public class JdbcLinkToChatRepositoryTest extends TestContainersConfiguration {

    @Autowired
    private JdbcLinkToChatRepository repository;

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
}

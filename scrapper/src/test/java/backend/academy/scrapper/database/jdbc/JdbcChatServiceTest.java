package backend.academy.scrapper.database.jdbc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.database.jdbc.repository.JdbcChatRepository;
import backend.academy.scrapper.database.jdbc.repository.JdbcLinkToChatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JdbcChatServiceTest {

    @Mock
    private JdbcChatRepository chatRepository;

    @Mock
    private JdbcLinkToChatRepository linkToChatRepository;

    @InjectMocks
    private JdbcChatService chatService;

    @Test
    public void testRegistrationClientSuccess() {
        long id = 1L;
        when(chatRepository.isClient(id)).thenReturn(false);

        assertThat(chatService.register(id)).isTrue();
        verify(chatRepository, times(1)).save(id);
    }

    @Test
    public void testRegistrationClientFailure() {
        long id = 1L;
        when(chatRepository.isClient(id)).thenReturn(true);

        assertThat(chatService.register(id)).isFalse();
        verify(chatRepository, times(0)).save(id);
    }

    @Test
    public void testUnregisterClientSuccess() {
        long id = 1L;
        when(chatRepository.isClient(id)).thenReturn(true);
        assertThat(chatService.unregister(id)).isTrue();
        verify(chatRepository, times(1)).delete(id);
        verify(linkToChatRepository, times(1)).unsubscribeAll(id);
    }

    @Test
    public void testUnregisterClientFailure() {
        long id = 1L;
        when(chatRepository.isClient(id)).thenReturn(false);
        assertThat(chatService.unregister(id)).isFalse();
        verify(chatRepository, times(0)).delete(id);
    }
}

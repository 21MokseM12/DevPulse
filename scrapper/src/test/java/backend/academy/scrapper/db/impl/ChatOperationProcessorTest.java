package backend.academy.scrapper.db.impl;

import backend.academy.scrapper.db.repository.ChatRepository;
import backend.academy.scrapper.db.repository.LinkToChatRepository;
import backend.academy.scrapper.service.impl.ChatOperationProcessorImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatOperationProcessorTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private LinkToChatRepository linkToChatRepository;

    @InjectMocks
    private ChatOperationProcessorImpl chatService;

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

package backend.academy.scrapper.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ChatService chatService;

    @BeforeEach
    public void setUp() {
        chatService = new ChatService(clientRepository);
    }

    @Test
    public void testRegistrationClientSuccess() {
        long id = 1L;
        when(clientRepository.isClient(id)).thenReturn(false);

        assertThat(chatService.register(id)).isTrue();
        verify(clientRepository, times(1)).register(id);
    }

    @Test
    public void testRegistrationClientFailure() {
        long id = 1L;
        when(clientRepository.isClient(id)).thenReturn(true);

        assertThat(chatService.register(id)).isFalse();
        verify(clientRepository, times(0)).register(id);
    }

    @Test
    public void testUnregisterClientSuccess() {
        long id = 1L;
        when(clientRepository.isClient(id)).thenReturn(true);
        assertThat(chatService.unregister(id)).isTrue();
        verify(clientRepository, times(1)).unregister(id);
    }

    @Test
    public void testUnregisterClientFailure() {
        long id = 1L;
        when(clientRepository.isClient(id)).thenReturn(false);
        assertThat(chatService.unregister(id)).isFalse();
        verify(clientRepository, times(0)).unregister(id);
    }
}

package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.db.repository.ChatRepository;
import backend.academy.scrapper.db.repository.LinkToChatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatOperationProcessorImplTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private LinkToChatRepository linkToChatRepository;

    @InjectMocks
    private ChatOperationProcessorImpl chatOperationProcessor;

    @Test
    void chatIsSubscribedOnLink_whenRepositoryReturnsTrue_returnsTrue() {
        Long chatId = 1001L;
        Long linkId = 2001L;
        when(linkToChatRepository.chatIsSubscribedOnLink(chatId, linkId)).thenReturn(true);

        boolean result = chatOperationProcessor.chatIsSubscribedOnLink(chatId, linkId);

        assertTrue(result);
        verify(linkToChatRepository).chatIsSubscribedOnLink(chatId, linkId);
        verify(linkToChatRepository, never()).subscribeChatOnLink(any(), any());
    }

    @Test
    void chatIsSubscribedOnLink_whenRepositoryReturnsFalse_returnsFalse() {
        Long chatId = 1002L;
        Long linkId = 2002L;
        when(linkToChatRepository.chatIsSubscribedOnLink(chatId, linkId)).thenReturn(false);

        boolean result = chatOperationProcessor.chatIsSubscribedOnLink(chatId, linkId);

        assertFalse(result);
        verify(linkToChatRepository).chatIsSubscribedOnLink(chatId, linkId);
        verify(linkToChatRepository, never()).subscribeChatOnLink(any(), any());
    }
}

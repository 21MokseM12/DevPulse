package backend.academy.bot.service.requests.mapper.impl;

import backend.academy.bot.service.commands.impl.stateful.TrackCommand;
import backend.academy.bot.service.commands.impl.stateful.sessions.TrackSessionManager;
import backend.academy.bot.service.requests.mapper.impl.TrackRequestMapper;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrackRequestMapperTest {

    @Mock
    private TrackCommand trackCommand;

    @Mock
    private TrackSessionManager trackSessionManager;

    @InjectMocks
    private TrackRequestMapper trackRequestMapper;

    private Update update;

    private Message message;

    @BeforeEach
    public void setUp() {
        update = mock(Update.class);
        message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
    }

    @Test
    public void whenCommandIsEqualsApiCommand_thenCanMapTrue() {
        when(message.text()).thenReturn("/track");
        when(trackCommand.apiCommand()).thenReturn("/track");
        when(trackSessionManager.hasSession(1L)).thenReturn(false);

        assertTrue(trackRequestMapper.canMap(update));
    }

    @Test
    public void whenCommandIsNotEqualsApiCommandAndHasSession_thenCanMap() {
        when(trackSessionManager.hasSession(1L)).thenReturn(true);

        assertTrue(trackRequestMapper.canMap(update));
    }

    @Test
    public void whenCommandIsNotEqualsApiCommandAndHasNotSession_thenCanMapFalse() {
        when(message.text()).thenReturn("/track");
        when(trackCommand.apiCommand()).thenReturn("/untrack");
        when(trackSessionManager.hasSession(1L)).thenReturn(false);

        assertFalse(trackRequestMapper.canMap(update));
    }
}

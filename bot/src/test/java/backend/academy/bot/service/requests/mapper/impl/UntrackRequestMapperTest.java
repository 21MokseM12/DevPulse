package backend.academy.bot.service.requests.mapper.impl;

import backend.academy.bot.model.requests.Request;
import backend.academy.bot.model.requests.UntrackRequest;
import backend.academy.bot.service.commands.impl.stateful.UntrackCommand;
import backend.academy.bot.service.commands.impl.stateful.sessions.UntrackSessionManager;
import backend.academy.bot.service.requests.mapper.impl.UntrackRequestMapper;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UntrackRequestMapperTest {

    @Mock
    private UntrackCommand untrackCommand;

    @Mock
    private UntrackSessionManager untrackSessionManager;

    @InjectMocks
    private UntrackRequestMapper untrackRequestMapper;

    @Test
    public void whenUpdateMessageCallbackIsNullBoth_thenReturnFalse() {
        Update update = new Update();
        assertFalse(untrackRequestMapper.canMap(update));
        assertNull(untrackRequestMapper.map(update));
    }

    @Test
    public void whenUpdateMessageIsNull_thenCanMapByHaveSession() {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);

        when(update.message()).thenReturn(null);
        when(update.callbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.data()).thenReturn("1_1");
        when(untrackSessionManager.hasSession(1L)).thenReturn(true);

        assertTrue(untrackRequestMapper.canMap(update));
    }

    @Test
    public void whenUpdateMessageIsNull_thenCanMapByHaveNotSession() {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);

        when(update.message()).thenReturn(null);
        when(update.callbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.data()).thenReturn("1_1");
        when(untrackSessionManager.hasSession(1L)).thenReturn(false);

        assertFalse(untrackRequestMapper.canMap(update));
    }

    @Test
    public void whenUpdateMessageIsNotNull_thenCanMapByTextIsCommand() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);

        when(update.message()).thenReturn(message);
        when(update.message().text()).thenReturn("/untrack");
        when(untrackCommand.apiCommand()).thenReturn("/untrack");

        assertTrue(untrackRequestMapper.canMap(update));
    }

    @Test
    public void whenUpdateMessageIsNotNull_thenCanMapBySession() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(update.message().text()).thenReturn("/track");
        when(update.message().chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(untrackCommand.apiCommand()).thenReturn("/untrack");
        when(untrackSessionManager.hasSession(1L)).thenReturn(true);

        assertTrue(untrackRequestMapper.canMap(update));
    }

    @Test
    public void mapByCallback() {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);

        when(update.callbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.data()).thenReturn("1_1");

        Request request = untrackRequestMapper.map(update);
        assertInstanceOf(UntrackRequest.class, request);
        UntrackRequest untrackRequest = (UntrackRequest) request;
        assertEquals(1L, request.getChatId());
        assertEquals("1_1", untrackRequest.getData());
    }

    @Test
    public void mapByTextMessage() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.text()).thenReturn("simple text");

        Request request = untrackRequestMapper.map(update);
        assertInstanceOf(UntrackRequest.class, request);
        UntrackRequest untrackRequest = (UntrackRequest) request;
        assertEquals(1L, request.getChatId());
        assertEquals("simple text", untrackRequest.getData());
    }
}

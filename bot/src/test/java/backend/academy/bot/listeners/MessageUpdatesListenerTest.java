package backend.academy.bot.listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.enums.Messages;
import backend.academy.bot.exceptions.InvalidCommandException;
import backend.academy.bot.service.UpdateProcessor;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MessageUpdatesListenerTest {

    @Mock
    private TelegramBot bot;

    @Mock
    private UpdateProcessor updateProcessor;

    @InjectMocks
    private MessageUpdatesListener listener;

    @Test
    void testProcessValidUpdate() {
        SendMessage testReply = new SendMessage(123L, "Test reply");

        Update update = mock(Update.class);
        when(updateProcessor.createReply(update)).thenReturn(testReply);

        listener.process(List.of(update));

        verify(updateProcessor, times(1)).createReply(update);
        verify(bot, times(1)).execute(testReply);
    }

    @Test
    void testProcessInvalidCommandException() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123L);

        when(updateProcessor.createReply(update)).thenThrow(new InvalidCommandException("Invalid command"));

        listener.process(List.of(update));

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(bot, times(1)).execute(captor.capture());

        SendMessage capturedMessage = captor.getValue();
        assertEquals(123L, capturedMessage.getParameters().get("chat_id"));
        assertEquals(
                Messages.INVALID_MESSAGE.toString(),
                capturedMessage.getParameters().get("text"));
    }
}

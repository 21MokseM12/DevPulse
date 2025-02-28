package backend.academy.bot.service.managers.stateless;

import backend.academy.bot.enums.Messages;
import backend.academy.bot.model.commands.Command;
import backend.academy.bot.service.ScrapperConnectionService;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StartCommandManagerTest {

    @Mock
    private ScrapperConnectionService scrapperConnectionService;

    @Mock
    private Command command;

    @InjectMocks
    private StartCommandManager startCommandManager;

    private final Update update = mock(Update.class);

    @BeforeEach
    public void setUp() {
        Message message = mock(Message.class);
        when(update.message()).thenReturn(message);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123L);
    }

    @Test
    public void testCreateReplySuccess() {
        SendMessage reply = startCommandManager.createReply(update);

        verify(scrapperConnectionService, times(1)).registerChat(any());
        assertEquals(123L, reply.getParameters().get("chat_id"));
        assertEquals(Messages.WELCOME_MESSAGE.toString(), reply.getParameters().get("text"));
    }

    @Test
    public void testCreateReplyWithBadRequestException() {
        doThrow(new BadRequestException(Messages.INVALID_MESSAGE.toString())).when(scrapperConnectionService).registerChat(any());
        SendMessage reply = startCommandManager.createReply(update);

        verify(scrapperConnectionService, times(1)).registerChat(any());
        assertEquals(123L, reply.getParameters().get("chat_id"));
        assertEquals(Messages.INVALID_MESSAGE.toString(), reply.getParameters().get("text"));
    }
}

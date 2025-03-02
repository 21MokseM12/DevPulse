package backend.academy.bot.service.managers.stateless;

import backend.academy.bot.enums.Messages;
import backend.academy.bot.service.ScrapperConnectionService;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.response.LinkResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListCommandManagerTest {

    @Mock
    private ScrapperConnectionService scrapperConnectionService;

    @InjectMocks
    private ListCommandManager listCommandManager;

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
    public void testFullListCommandSuccess() {
        LinkResponse link = new LinkResponse(1L, URI.create("link"), List.of("tag"), List.of("filter"));
        when(scrapperConnectionService.getAllLinks(update.message().chat().id()))
            .thenReturn(List.of(link));

        SendMessage reply = listCommandManager.createReply(update);
        verify(scrapperConnectionService, times(1)).getAllLinks(update.message().chat().id());
        assertEquals(123L, reply.getParameters().get("chat_id"));
        assertEquals(
            "Список отслеживаемых ссылок:\nlink\n",
            reply.getParameters().get("text")
        );
    }

    @Test
    public void testEmptyListCommandSuccess() {
        when(scrapperConnectionService.getAllLinks(update.message().chat().id()))
            .thenReturn(List.of());

        SendMessage reply = listCommandManager.createReply(update);
        verify(scrapperConnectionService, times(1)).getAllLinks(update.message().chat().id());
        assertEquals(123L, reply.getParameters().get("chat_id"));
        assertEquals(Messages.EMPTY_LINK_LIST.toString(), reply.getParameters().get("text"));
    }

    @Test
    public void testThrowsBadRequestException() {
        when(scrapperConnectionService.getAllLinks(update.message().chat().id()))
            .thenThrow(new BadRequestException("Bad request"));

        SendMessage reply = listCommandManager.createReply(update);
        verify(scrapperConnectionService, times(1)).getAllLinks(update.message().chat().id());
        assertEquals(123L, reply.getParameters().get("chat_id"));
        assertEquals("Bad request", reply.getParameters().get("text"));

    }
}

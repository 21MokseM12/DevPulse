 package backend.academy.bot.service.commands.managers.stateless;

 import backend.academy.bot.enums.Messages;
 import backend.academy.bot.model.requests.Request;
 import backend.academy.bot.model.requests.StatelessRequest;
 import backend.academy.bot.model.requests.TrackRequest;
 import backend.academy.bot.service.ScrapperConnectionService;
 import backend.academy.bot.service.commands.impl.stateless.ListCommand;
 import com.pengrad.telegrambot.request.SendMessage;
 import java.net.URI;
 import java.util.List;
 import org.junit.jupiter.api.Test;
 import org.junit.jupiter.api.extension.ExtendWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.junit.jupiter.MockitoExtension;
 import scrapper.bot.connectivity.exceptions.BadRequestException;
 import scrapper.bot.connectivity.model.response.LinkResponse;
 import static org.junit.jupiter.api.Assertions.assertEquals;
 import static org.junit.jupiter.api.Assertions.assertFalse;
 import static org.junit.jupiter.api.Assertions.assertTrue;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;

 @ExtendWith(MockitoExtension.class)
 public class ListCommandManagerTest {

    @Mock
    private ScrapperConnectionService scrapperConnectionService;

    @Mock
    private ListCommand listCommand;

    @InjectMocks
    private ListCommandManager listCommandManager;

    @Test
    public void testFullListCommandSuccess() {
        StatelessRequest request = new StatelessRequest(123L, "/list");
        LinkResponse link = new LinkResponse(1L, URI.create("link"), List.of("tag"), List.of("filter"));
        when(scrapperConnectionService.getAllLinks(request.getChatId()))
                .thenReturn(List.of(link));

        SendMessage reply = listCommandManager.createReply(request);
        verify(scrapperConnectionService, times(1))
                .getAllLinks(request.getChatId());
        assertEquals(123L, reply.getParameters().get("chat_id"));
        assertEquals(
                "Список отслеживаемых ссылок:\nlink\n", reply.getParameters().get("text"));
    }

    @Test
    public void testEmptyListCommandSuccess() {
        StatelessRequest request = new StatelessRequest(123L, "/list");
        when(scrapperConnectionService.getAllLinks(request.getChatId()))
                .thenReturn(List.of());

        SendMessage reply = listCommandManager.createReply(request);
        verify(scrapperConnectionService, times(1))
                .getAllLinks(request.getChatId());
        assertEquals(123L, reply.getParameters().get("chat_id"));
        assertEquals(Messages.EMPTY_LINK_LIST.toString(), reply.getParameters().get("text"));
    }

    @Test
    public void testThrowsBadRequestException() {
        StatelessRequest request = new StatelessRequest(123L, "/list");
        when(scrapperConnectionService.getAllLinks(request.getChatId()))
                .thenThrow(new BadRequestException("Bad request"));

        SendMessage reply = listCommandManager.createReply(request);
        verify(scrapperConnectionService, times(1))
                .getAllLinks(request.getChatId());
        assertEquals(123L, reply.getParameters().get("chat_id"));
        assertEquals("Bad request", reply.getParameters().get("text"));
    }

     @Test
     public void testCanProcess_whenRequestIsNotStatelessRequest_thenFailure() {
         Request request = new TrackRequest(123L, "/untrack");
         assertFalse(listCommandManager.canProcess(request));
     }

     @Test
     public void testCanProcess_whenRequestDataIsAPICommand_thenSuccess() {
         Request request = new StatelessRequest(123L, "/list");

         when(listCommand.apiCommand()).thenReturn("/list");

         assertTrue(listCommandManager.canProcess(request));
     }

     @Test
     public void testCanProcess_whenRequestDataIsNotAPICommand_thenFailure() {
         Request request = new StatelessRequest(123L, "/ttrack");
         assertFalse(listCommandManager.canProcess(request));
     }
 }

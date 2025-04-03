package backend.academy.bot.service.commands.managers.stateless;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.enums.Messages;
import backend.academy.bot.model.requests.Request;
import backend.academy.bot.model.requests.StatelessRequest;
import backend.academy.bot.model.requests.TrackRequest;
import backend.academy.bot.service.ScrapperConnectionService;
import backend.academy.bot.service.commands.impl.stateless.StartCommand;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import scrapper.bot.connectivity.exceptions.BadRequestException;

@ExtendWith(MockitoExtension.class)
public class StartCommandManagerTest {

    @Mock
    private ScrapperConnectionService scrapperConnectionService;

    @Mock
    private StartCommand startcommand;

    @InjectMocks
    private StartCommandManager startCommandManager;

    @Test
    public void testCreateReplySuccess() {
        StatelessRequest request = new StatelessRequest(123L, "/start");
        SendMessage reply = startCommandManager.createReply(request);

        verify(scrapperConnectionService, times(1)).registerChat(any());
        assertEquals(123L, reply.getParameters().get("chat_id"));
        assertEquals(Messages.WELCOME_MESSAGE.toString(), reply.getParameters().get("text"));
    }

    @Test
    public void testCreateReplyWithBadRequestException() {
        StatelessRequest request = new StatelessRequest(123L, "/start");
        doThrow(new BadRequestException(Messages.INVALID_MESSAGE.toString()))
                .when(scrapperConnectionService)
                .registerChat(any());
        SendMessage reply = startCommandManager.createReply(request);

        verify(scrapperConnectionService, times(1)).registerChat(any());
        assertEquals(123L, reply.getParameters().get("chat_id"));
        assertEquals(Messages.INVALID_MESSAGE.toString(), reply.getParameters().get("text"));
    }

    @Test
    public void testCanProcess_whenRequestIsNotStatelessRequest_thenFailure() {
        Request request = new TrackRequest(123L, "/untrack");
        assertFalse(startCommandManager.canProcess(request));
    }

    @Test
    public void testCanProcess_whenRequestDataIsAPICommand_thenSuccess() {
        Request request = new StatelessRequest(123L, "/start");

        when(startcommand.apiCommand()).thenReturn("/start");

        assertTrue(startCommandManager.canProcess(request));
    }

    @Test
    public void testCanProcess_whenRequestDataIsNotAPICommand_thenFailure() {
        Request request = new StatelessRequest(123L, "/ttrack");
        assertFalse(startCommandManager.canProcess(request));
    }
}

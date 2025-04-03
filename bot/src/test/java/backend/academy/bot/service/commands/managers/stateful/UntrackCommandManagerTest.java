package backend.academy.bot.service.commands.managers.stateful;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.bot.enums.Messages;
import backend.academy.bot.model.requests.Request;
import backend.academy.bot.model.requests.StatelessRequest;
import backend.academy.bot.model.requests.TrackRequest;
import backend.academy.bot.model.requests.UntrackRequest;
import backend.academy.bot.service.ScrapperConnectionService;
import backend.academy.bot.service.commands.Command;
import backend.academy.bot.service.commands.impl.stateful.sessions.UntrackSessionManager;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import scrapper.bot.connectivity.model.response.LinkResponse;

@ExtendWith(MockitoExtension.class)
class UntrackCommandManagerTest {

    @Mock
    private UntrackSessionManager untrackSessionManager;

    @Mock
    private ScrapperConnectionService scrapperConnectionService;

    @Mock
    private Command untrackCommand;

    @InjectMocks
    private UntrackCommandManager untrackCommandManager;

    private final long chatId = 12345L;

    @BeforeEach
    void setUp() {
        lenient().when(untrackSessionManager.hasSession(chatId)).thenReturn(false);
    }

    @Test
    void testCreateReply_WithInvalidRequest() {
        Request request = mock(Request.class);
        when(request.getChatId()).thenReturn(chatId);
        SendMessage response = untrackCommandManager.createReply(request);
        assertEquals(Messages.ERROR.toString(), response.getParameters().get("text"));
    }

    @Test
    void testCreateReply_WhenNoSubscribedLinks() {
        UntrackRequest request = mock(UntrackRequest.class);
        when(request.getChatId()).thenReturn(chatId);
        when(scrapperConnectionService.getAllLinks(chatId)).thenReturn(List.of());
        SendMessage response = untrackCommandManager.createReply(request);
        assertEquals(
                Messages.EMPTY_LINK_LIST.toString(), response.getParameters().get("text"));
    }

    @Test
    void testCreateReply_WithSubscribedLinks() {
        UntrackRequest request = mock(UntrackRequest.class);
        when(request.getChatId()).thenReturn(chatId);
        when(scrapperConnectionService.getAllLinks(chatId))
                .thenReturn(List.of(new LinkResponse(1L, URI.create("http://example.com"), Set.of(), Set.of())));
        SendMessage response = untrackCommandManager.createReply(request);
        assertEquals(
                Messages.SEND_LINK_MESSAGE_UNTRACK.toString(),
                response.getParameters().get("text"));
    }

    @Test
    void testCreateReply_WhenSessionExistsButNoCallbackQuery() {
        when(untrackSessionManager.hasSession(chatId)).thenReturn(true);
        UntrackRequest request = mock(UntrackRequest.class);
        when(request.getChatId()).thenReturn(chatId);
        when(request.isCallbackQuery()).thenReturn(false);
        SendMessage response = untrackCommandManager.createReply(request);
        assertEquals(Messages.ERROR.toString(), response.getParameters().get("text"));
    }

    @Test
    void testCreateReply_WithValidUntrackRequest() {
        when(untrackSessionManager.hasSession(chatId)).thenReturn(true);
        UntrackRequest request = mock(UntrackRequest.class);
        when(request.getChatId()).thenReturn(chatId);
        when(request.isCallbackQuery()).thenReturn(true);
        when(request.getData()).thenReturn("1_0");
        when(scrapperConnectionService.unsubscribeLink(1L, List.of(), 0L)).thenReturn(true);
        SendMessage response = untrackCommandManager.createReply(request);
        assertEquals(
                Messages.DELETE_SUBSCRIBE_MESSAGE.toString(),
                response.getParameters().get("text"));
    }

    @Test
    void testHasState() {
        when(untrackSessionManager.hasSession(chatId)).thenReturn(true);
        assertTrue(untrackCommandManager.hasState(chatId));
    }

    @Test
    public void testCanProcess_whenRequestIsNotUntrackRequest_thenFailure() {
        Request request = new StatelessRequest(123L, "/untrack");
        assertFalse(untrackCommandManager.canProcess(request));
    }

    @Test
    public void testCanProcess_whenRequestDataIsAPICommand_thenSuccess() {
        Request request = new UntrackRequest(123L, "/untrack", false);

        when(untrackCommand.apiCommand()).thenReturn("/untrack");

        assertTrue(untrackCommandManager.canProcess(request));
    }

    @Test
    public void testCanProcess_whenRequestDataIsNotAPICommand_thenFailure() {
        Request request = new TrackRequest(123L, "/ttrack");
        assertFalse(untrackCommandManager.canProcess(request));
    }

    @Test
    public void testCanProcess_whenRequestHaveSession_thenSuccess() {
        Request request = new UntrackRequest(123L, "/track", false);

        when(untrackSessionManager.hasSession(123L)).thenReturn(true);

        assertTrue(untrackCommandManager.canProcess(request));
    }

    @Test
    public void testCanProcess_whenRequestHaveNotSession_thenFailure() {
        Request request = new UntrackRequest(123L, "/track", false);

        when(untrackSessionManager.hasSession(123L)).thenReturn(false);

        assertFalse(untrackCommandManager.canProcess(request));
    }
}

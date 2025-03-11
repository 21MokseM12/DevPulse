package backend.academy.bot.service.commands.managers.stateful;

import backend.academy.bot.enums.TrackCommandStates;
import backend.academy.bot.model.entity.LinkDTO;
import backend.academy.bot.model.requests.TrackRequest;
import backend.academy.bot.service.ScrapperConnectionService;
import backend.academy.bot.service.commands.Command;
import backend.academy.bot.service.commands.impl.stateful.sessions.TrackSessionManager;
import backend.academy.bot.utils.LinkValidator;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackCommandManagerTest {

    @Mock
    private Command trackCommand;

    @Mock
    private ScrapperConnectionService scrapperConnectionService;

    @Mock
    private TrackSessionManager trackSessionManager;

    @InjectMocks
    private TrackCommandManager trackCommandManager;

    private static final long CHAT_ID = 12345L;

    @BeforeEach
    void setup() {
        trackCommandManager = new TrackCommandManager(trackCommand, scrapperConnectionService, trackSessionManager);
    }

    @Test
    void testCreateReply_NewSession() {
        TrackRequest request = new TrackRequest(CHAT_ID, "someLink");
        when(trackSessionManager.hasSession(CHAT_ID)).thenReturn(false);

        SendMessage response = trackCommandManager.createReply(request);

        assertEquals(TrackCommandStates.LINK.successMessage(), response.getParameters().get("text"));
        verify(trackSessionManager).createSession(CHAT_ID);
    }

    @Test
    void testCreateReply_InvalidLink() {
        TrackRequest request = new TrackRequest(CHAT_ID, "invalidLink");
        when(trackSessionManager.hasSession(CHAT_ID)).thenReturn(true);
        when(trackSessionManager.getSession(CHAT_ID)).thenReturn(TrackCommandStates.LINK);

        SendMessage response = trackCommandManager.createReply(request);

        assertEquals(TrackCommandStates.LINK.errorMessage(), response.getParameters().get("text"));
    }

    @Test
    void testCreateReply_ValidLink() {
        TrackRequest request = new TrackRequest(CHAT_ID, "http://valid.link");
        when(trackSessionManager.hasSession(CHAT_ID)).thenReturn(true);
        when(trackSessionManager.getSession(CHAT_ID)).thenReturn(TrackCommandStates.LINK);

        mockStatic(LinkValidator.class).when(() -> LinkValidator.isValid(request.getData())).thenReturn(true);

        SendMessage response = trackCommandManager.createReply(request);

        assertEquals(TrackCommandStates.LINK.successMessage(), response.getParameters().get("text"));
    }

    @Test
    void testCreateReply_Filters_BadRequestException() throws BadRequestException {
        TrackRequest request = new TrackRequest(CHAT_ID, "filters");
        when(trackSessionManager.hasSession(CHAT_ID)).thenReturn(true);
        when(trackSessionManager.getSession(CHAT_ID)).thenReturn(TrackCommandStates.FILTERS);

        doThrow(new BadRequestException("Bad Request"))
            .when(scrapperConnectionService).subscribeLink(eq(CHAT_ID), any(LinkDTO.class));

        SendMessage response = trackCommandManager.createReply(request);

        assertEquals("Bad Request", response.getParameters().get("text"));
    }

    @Test
    void testHasState() {
        when(trackSessionManager.hasSession(CHAT_ID)).thenReturn(true);

        assertTrue(trackCommandManager.hasState(CHAT_ID));
    }

    @Test
    void testGetCommand() {
        assertEquals(trackCommand, trackCommandManager.getCommand());
    }
}

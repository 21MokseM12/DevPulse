package backend.academy.bot.service.managers.stateful;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import backend.academy.bot.enums.Messages;
import backend.academy.bot.enums.TrackCommandStates;
import backend.academy.bot.service.ScrapperConnectionService;
import backend.academy.bot.service.commands.managers.stateful.TrackCommandManager;
import backend.academy.bot.utils.LinkValidator;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import scrapper.bot.connectivity.exceptions.BadRequestException;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TrackCommandManagerTest {

    @Mock
    private ScrapperConnectionService scrapperConnectionService;

    @InjectMocks
    private TrackCommandManager trackCommandManager;

    private Message message;

    private Update mockUpdate(long chatId) {
        Chat chat = mock(Chat.class);
        message = mock(Message.class);
        Update update = mock(Update.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);

        return update;
    }

    @Test
    @Order(1)
    void testFirstState_Linking() {
        long chatId = 123L;
        Update update = mockUpdate(chatId);

        SendMessage response = trackCommandManager.createReply(update);

        assertEquals(
            TrackCommandStates.LINK.successMessage(),
                response.getParameters().get("text"));
        assertTrue(trackCommandManager.hasState(chatId));
    }

    @Test
    @Order(2)
    void testInvalidLink() {
        long chatId = 123L;
        Update update = mockUpdate(chatId);
        when(message.text()).thenReturn("invalid link");

        try (MockedStatic<LinkValidator> mockedStatic = mockStatic(LinkValidator.class)) {
            mockedStatic.when(() -> LinkValidator.isValid("invalid link")).thenReturn(false);
            SendMessage response = trackCommandManager.createReply(update);
            assertEquals(
                TrackCommandStates.LINK.errorMessage(),
                    response.getParameters().get("text"));
        }
    }

    @Test
    @Order(3)
    void testValidLink() throws BadRequestException {
        long chatId = 123L;
        Update update = mockUpdate(chatId);
        when(message.text()).thenReturn("https://example.com");

        try (MockedStatic<LinkValidator> mockedStatic = mockStatic(LinkValidator.class)) {
            mockedStatic
                    .when(() -> LinkValidator.isValid("https://example.com"))
                    .thenReturn(true);
            SendMessage response = trackCommandManager.createReply(update);
            assertEquals(
                TrackCommandStates.TAGS.successMessage(),
                    response.getParameters().get("text"));
        }
    }

    @Test
    @Order(4)
    public void testTagsSuccess() {
        long chatId = 123L;
        Update update = mockUpdate(chatId);
        when(message.text()).thenReturn("tag1 tag2");

        SendMessage response = trackCommandManager.createReply(update);

        assertEquals(
            TrackCommandStates.FILTERS.successMessage(),
                response.getParameters().get("text"));
    }

    @Test
    @Order(5)
    public void testFiltersThrowsBadRequestException() {
        long chatId = 123L;
        Update update = mockUpdate(chatId);
        when(message.text()).thenReturn("filter1 filter2");

        doThrow(new BadRequestException("Bad request"))
                .when(scrapperConnectionService)
                .subscribeLink(anyLong(), any());

        SendMessage reply = trackCommandManager.createReply(update);
        assertEquals("Bad request", reply.getParameters().get("text"));
    }

    @Test
    @Order(6)
    public void testFiltersSuccess() {
        long chatId = 123L;
        Update update = mockUpdate(chatId);
        when(message.text()).thenReturn("filter1 filter2");

        SendMessage reply = trackCommandManager.createReply(update);
        assertEquals(
                Messages.SUCCESS_SUBSCRIBE_LINK.toString(),
                reply.getParameters().get("text"));
        assertFalse(trackCommandManager.hasState(chatId));
    }
}

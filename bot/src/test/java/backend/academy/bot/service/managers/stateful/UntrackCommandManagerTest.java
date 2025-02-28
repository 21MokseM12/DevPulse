package backend.academy.bot.service.managers.stateful;

import backend.academy.bot.enums.Messages;
import backend.academy.bot.service.ScrapperConnectionService;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import scrapper.bot.connectivity.model.Link;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UntrackCommandManagerTest {

    @Mock
    private ScrapperConnectionService scrapperConnectionService;

    @InjectMocks
    private UntrackCommandManager untrackCommandManager;

    private Update update;

    private Chat chat = mock(Chat.class);

    @BeforeEach
    void setUp() {
        update = mock(Update.class);
        Message message = mock(Message.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
    }

    @Test
    void createReply_ShouldReturnKeyboardOnFirstCallOddNumberLinks() {
        when(chat.id()).thenReturn(1L);

        when(scrapperConnectionService.getAllLinks(1L)).thenReturn(List.of(
            new Link(1, "https://example.com"),
            new Link(2, "https://example.ru"),
            new Link(3, "https://example.en")
        ));

        SendMessage response = untrackCommandManager.createReply(update);

        assertEquals(Messages.SEND_LINK_MESSAGE_UNTRACK.toString(), response.getParameters().get("text"));
        assertInstanceOf(InlineKeyboardMarkup.class, response.getParameters().get("reply_markup"));
    }

    @Test
    void createReply_ShouldReturnKeyboardOnFirstCallEvenNumberLinks() {
        when(chat.id()).thenReturn(2L);

        when(scrapperConnectionService.getAllLinks(2L)).thenReturn(List.of(
            new Link(1, "https://example.com"),
            new Link(2, "https://example.ru")
        ));

        SendMessage response = untrackCommandManager.createReply(update);

        assertEquals(Messages.SEND_LINK_MESSAGE_UNTRACK.toString(), response.getParameters().get("text"));
        assertInstanceOf(InlineKeyboardMarkup.class, response.getParameters().get("reply_markup"));
    }

    @Test
    void createReply_ShouldReturnErrorIfNoCallbackQuery() {
        when(chat.id()).thenReturn(3L);

        untrackCommandManager.createReply(update); // Первый вызов добавляет в состояние

        SendMessage response = untrackCommandManager.createReply(update);

        assertEquals(Messages.ERROR.toString(), response.getParameters().get("text"));
    }

    @Test
    void createReply_ShouldUnsubscribeSuccessfully() {
        when(chat.id()).thenReturn(4L);

        when(update.callbackQuery()).thenReturn(mock(CallbackQuery.class));
        when(update.callbackQuery().data()).thenReturn("1");
        when(scrapperConnectionService.getAllLinks(4L)).thenReturn(List.of(new Link(1, "https://example.com")));
        when(scrapperConnectionService.unsubscribeLink(eq(4L), anyList(), eq(1))).thenReturn(true);

        untrackCommandManager.createReply(update); // Первый вызов добавляет в состояние
        SendMessage response = untrackCommandManager.createReply(update);

        assertEquals(Messages.DELETE_SUBSCRIBE_MESSAGE.toString(), response.getParameters().get("text"));
    }

    @Test
    void hasState_ShouldReturnTrueAfterFirstCall() {
        when(chat.id()).thenReturn(4L);
        untrackCommandManager.createReply(update);
        assertTrue(untrackCommandManager.hasState(4L));
    }

    @Test
    void hasState_ShouldReturnFalseAfterSuccessfulUnsubscribe() {
        when(chat.id()).thenReturn(4L);

        when(update.callbackQuery()).thenReturn(mock(CallbackQuery.class));
        when(update.callbackQuery().data()).thenReturn("1");
        when(scrapperConnectionService.getAllLinks(4L)).thenReturn(List.of(new Link(1, "https://example.com")));
        when(scrapperConnectionService.unsubscribeLink(eq(4L), anyList(), eq(1))).thenReturn(true);

        untrackCommandManager.createReply(update);
        untrackCommandManager.createReply(update);

        assertFalse(untrackCommandManager.hasState(123L));
    }
}


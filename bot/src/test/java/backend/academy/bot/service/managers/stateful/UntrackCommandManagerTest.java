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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import scrapper.bot.connectivity.model.response.LinkResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UntrackCommandManagerTest {

    @Mock
    private ScrapperConnectionService scrapperConnectionService;

    @InjectMocks
    private UntrackCommandManager untrackCommandManager;

    private Update update;

    private final Chat chat = mock(Chat.class);

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
            new LinkResponse(1L, "https://example.com", List.of("tag1"), List.of("filter1")),
            new LinkResponse(2L, "https://example.ru", List.of("tag2"), List.of("filter2")),
            new LinkResponse(3L, "https://example.en", List.of("tag3"), List.of("filter3"))
        ));

        SendMessage response = untrackCommandManager.createReply(update);

        assertEquals(Messages.SEND_LINK_MESSAGE_UNTRACK.toString(), response.getParameters().get("text"));
        assertInstanceOf(InlineKeyboardMarkup.class, response.getParameters().get("reply_markup"));
    }

    @Test
    void createReply_ShouldReturnKeyboardOnFirstCallEvenNumberLinks() {
        when(chat.id()).thenReturn(2L);

        when(scrapperConnectionService.getAllLinks(2L)).thenReturn(List.of(
            new LinkResponse(1L, "https://example.com", List.of("tag1"), List.of("filter1")),
            new LinkResponse(2L, "https://example.ru", List.of("tag2"), List.of("filter2"))
        ));

        SendMessage response = untrackCommandManager.createReply(update);

        assertEquals(Messages.SEND_LINK_MESSAGE_UNTRACK.toString(), response.getParameters().get("text"));
        assertInstanceOf(InlineKeyboardMarkup.class, response.getParameters().get("reply_markup"));
    }

    @Test
    void createReply_ShouldReturnEmptyUntrackLinkListMessageIfNoCallbackQuery() {
        when(chat.id()).thenReturn(3L);

        untrackCommandManager.createReply(update); // Первый вызов добавляет в состояние

        SendMessage response = untrackCommandManager.createReply(update);

        assertEquals(Messages.EMPTY_LINK_LIST.toString(), response.getParameters().get("text"));
    }

    @Test
    @Order(2)
    void createReply_ShouldUnsubscribeSuccessfully() {
        when(chat.id()).thenReturn(4L);

        when(update.callbackQuery()).thenReturn(mock(CallbackQuery.class));
        when(update.callbackQuery().data()).thenReturn("4_1");
        when(scrapperConnectionService.getAllLinks(4L))
            .thenReturn(List.of(new LinkResponse(1L, "https://example.com", List.of("tag1"), List.of("filter1"))));
        when(scrapperConnectionService.unsubscribeLink(eq(4L), anyList(), eq(1))).thenReturn(true);

        SendMessage response = untrackCommandManager.createReply(update);

        assertEquals(Messages.DELETE_SUBSCRIBE_MESSAGE.toString(), response.getParameters().get("text"));
        assertFalse(untrackCommandManager.hasState(4L));
    }

    @Test
    @Order(1)
    void hasState_ShouldReturnTrueAfterFirstCall() {
        when(chat.id()).thenReturn(4L);
        when(scrapperConnectionService.getAllLinks(4L))
            .thenReturn(List.of(new LinkResponse(1L, "https://example.com", List.of("tag1"), List.of("filter1"))));

        untrackCommandManager.createReply(update);
        assertTrue(untrackCommandManager.hasState(4L));
    }

    @Test
    public void createReply_ShouldReturnEmptyLinkListToUntrackMessage() {
        when(chat.id()).thenReturn(5L);
        when(scrapperConnectionService.getAllLinks(5L)).thenReturn(List.of());

        SendMessage reply = untrackCommandManager.createReply(update);

        assertEquals(Messages.EMPTY_LINK_LIST.toString(), reply.getParameters().get("text"));
        assertNull(reply.getParameters().get("reply_markup"));
    }
}


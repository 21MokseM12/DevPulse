package backend.academy.bot.service.commands.managers.stateful;

import backend.academy.bot.enums.Messages;
import backend.academy.bot.model.requests.Request;
import backend.academy.bot.model.requests.UntrackRequest;
import backend.academy.bot.service.ScrapperConnectionService;
import backend.academy.bot.service.commands.Command;
import backend.academy.bot.service.commands.impl.stateful.sessions.UntrackSessionManager;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import scrapper.bot.connectivity.model.response.LinkResponse;

@Component
public class UntrackCommandManager implements StatefulCommandManager {

    private final UntrackSessionManager untrackSessionManager;

    private final ScrapperConnectionService scrapperConnectionService;

    private final Command untrackCommand;

    @Autowired
    public UntrackCommandManager(
        UntrackSessionManager untrackSessionManager,
        @Qualifier("untrackCommand") Command untrackCommand,
        ScrapperConnectionService scrapperConnectionService
    ) {
        this.untrackSessionManager = untrackSessionManager;
        this.scrapperConnectionService = scrapperConnectionService;
        this.untrackCommand = untrackCommand;
    }

    @Override
    public SendMessage createReply(Request request) {
        if (!(request instanceof UntrackRequest untrackRequest)) {
            return new SendMessage(request.getChatId(), Messages.ERROR.toString());
        } else {
            List<LinkResponse> subscribedLinks = scrapperConnectionService.getAllLinks(untrackRequest.getChatId());
            if (!untrackSessionManager.hasSession(untrackRequest.getChatId())) {
                SendMessage reply;
                if (subscribedLinks.isEmpty()) {
                    reply = new SendMessage(untrackRequest.getChatId(), Messages.EMPTY_LINK_LIST.toString());
                } else {
                    untrackSessionManager.createSession(untrackRequest.getChatId());
                    reply = new SendMessage(untrackRequest.getChatId(), Messages.SEND_LINK_MESSAGE_UNTRACK.toString());
                    reply.replyMarkup(generateKeyboard(
                        subscribedLinks, untrackRequest.getChatId()
                    ));
                }
                return reply;
            } else {
                if (!untrackRequest.isCallbackQuery()) {
                    return new SendMessage(untrackRequest.getChatId(), Messages.ERROR.toString());
                }
                if (!scrapperConnectionService.unsubscribeLink(
                    Long.parseLong(untrackRequest.getData().split("_")[0]),
                    subscribedLinks,
                    Integer.parseInt(untrackRequest.getData().split("_")[1]))) {
                    return new SendMessage(untrackRequest.getChatId(), Messages.ERROR.toString());
                }
                untrackSessionManager.deleteSession(untrackRequest.getChatId());
                return new SendMessage(untrackRequest.getChatId(), Messages.DELETE_SUBSCRIBE_MESSAGE.toString());
            }
        }
    }

    private Keyboard generateKeyboard(List<LinkResponse> links, Long chatId) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        for (LinkResponse link : links) {
            var button = new InlineKeyboardButton();
            button.setText(link.url().toString());
            button.callbackData(String.valueOf(chatId).concat("_").concat(String.valueOf(link.id())));

            keyboard.addRow(button);
        }
        return keyboard;
    }

    @Override
    public boolean hasState(long chatId) {
        return untrackSessionManager.hasSession(chatId);
    }

    @Override
    public Command getCommand() {
        return untrackCommand;
    }
}

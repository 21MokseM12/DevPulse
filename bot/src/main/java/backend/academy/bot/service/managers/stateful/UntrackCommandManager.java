package backend.academy.bot.service.managers.stateful;

import backend.academy.bot.enums.Messages;
import backend.academy.bot.commands.Command;
import backend.academy.bot.service.ScrapperConnectionService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import scrapper.bot.connectivity.model.response.LinkResponse;

@Component
public class UntrackCommandManager implements StatefulCommandManager {

    private static final Set<Long> STATES;

    @Autowired
    private ScrapperConnectionService scrapperConnectionService;

    @Autowired
    @Qualifier("untrackCommand")
    private Command untrackCommand;

    static {
        STATES = new HashSet<>();
    }

    @Override
    public SendMessage createReply(Update update) {
        long chatId = update.message() == null ?
            Long.parseLong(update.callbackQuery().data().split("_")[0]) :
            update.message().chat().id();
        List<LinkResponse> subscribedLinks = scrapperConnectionService.getAllLinks(chatId);
        if (!STATES.contains(chatId)) {
            SendMessage reply;
            if (subscribedLinks.isEmpty()) {
                reply = new SendMessage(
                    update.message().chat().id(),
                    Messages.EMPTY_LINK_LIST.toString()
                );
            } else {
                STATES.add(update.message().chat().id());
                reply = new SendMessage(
                    update.message().chat().id(),
                    Messages.SEND_LINK_MESSAGE_UNTRACK.toString()
                );
                reply.replyMarkup(generateKeyboard(subscribedLinks, update.message().chat().id()));
            }
            return reply;
        } else {
            if (update.callbackQuery() == null) {
                return new SendMessage(chatId, Messages.ERROR.toString());
            }
            String callbackData = update.callbackQuery().data();
            if (!scrapperConnectionService.unsubscribeLink(
                Long.parseLong(callbackData.split("_")[0]),
                subscribedLinks,
                Integer.parseInt(callbackData.split("_")[1])
            )) {
                return new SendMessage(chatId, Messages.ERROR.toString());
            }
            STATES.remove(chatId);
            return new SendMessage(chatId, Messages.DELETE_SUBSCRIBE_MESSAGE.toString());
        }
    }

    private Keyboard generateKeyboard(List<LinkResponse> links, Long chatId) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        for (LinkResponse link : links) {
            var button = new InlineKeyboardButton();
            button.setText(link.url());
            button.callbackData(
                String.valueOf(chatId)
                    .concat("_")
                    .concat(String.valueOf(link.id()))
            );

            keyboard.addRow(button);
        }
        return keyboard;
    }

    @Override
    public boolean hasState(long chatId) {
        return STATES.contains(chatId);
    }

    @Override
    public Command getCommand() {
        return untrackCommand;
    }
}

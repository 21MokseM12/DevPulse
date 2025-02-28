package backend.academy.bot.service.managers.stateful;

import backend.academy.bot.enums.Messages;
import backend.academy.bot.model.commands.Command;
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
import scrapper.bot.connectivity.model.Link;

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
        List<Link> subscribedLinks = scrapperConnectionService.getAllLinks(update.message().chat().id());
        if (!STATES.contains(update.message().chat().id())) {
            STATES.add(update.message().chat().id());
            SendMessage reply = new SendMessage(
                update.message().chat().id(),
                Messages.SEND_LINK_MESSAGE_UNTRACK.toString()
            );
            reply.replyMarkup(generateKeyboard(subscribedLinks));
            return reply;
        } else {
            if (update.callbackQuery() == null) {
                return new SendMessage(update.message().chat().id(), Messages.ERROR.toString());
            }
            String callbackData = update.callbackQuery().data();
            if (!scrapperConnectionService.unsubscribeLink(
                update.message().chat().id(),
                subscribedLinks,
                Integer.parseInt(callbackData)
            )) {
                return new SendMessage(update.message().chat().id(), Messages.ERROR.toString());
            }
            STATES.remove(update.message().chat().id());
            return new SendMessage(update.message().chat().id(), Messages.DELETE_SUBSCRIBE_MESSAGE.toString());
        }
    }

    private Keyboard generateKeyboard(List<Link> links) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        for (Link link : links) {
            var button = new InlineKeyboardButton();
            button.setText(link.uri());
            button.callbackData(String.valueOf(link.id()));

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

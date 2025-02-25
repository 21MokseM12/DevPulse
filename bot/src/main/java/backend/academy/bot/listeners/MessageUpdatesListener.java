package backend.academy.bot.listeners;

import backend.academy.bot.enums.Messages;
import backend.academy.bot.exceptions.InvalidCommandException;
import backend.academy.bot.service.UpdateProcessor;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageUpdatesListener implements UpdatesListener {

    private final TelegramBot bot;

    private final UpdateProcessor updateProcessor;

    @Autowired
    public MessageUpdatesListener(TelegramBot bot, UpdateProcessor updateProcessor) {
        bot.setUpdatesListener(this);
        this.bot = bot;
        this.updateProcessor = updateProcessor;
    }

    @Override
    public int process(List<Update> updates) {
        updates
            .forEach(update -> {
                try {
                    bot.execute(updateProcessor.createReply(update));
                } catch (InvalidCommandException e) {
                    log.error("Error occur while message handling: ", e);
                    bot.execute(new SendMessage(
                        update.message().chat().id(),
                        Messages.INVALID_MESSAGE.toString()
                    ));
                }
            });
        return CONFIRMED_UPDATES_ALL;
    }
}

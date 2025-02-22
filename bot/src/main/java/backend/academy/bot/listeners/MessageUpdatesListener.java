package backend.academy.bot.listeners;

import backend.academy.bot.model.commands.InvalidCommandException;
import backend.academy.bot.service.MessageProcessor;
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

    private static final String INVALID_MESSAGE = "Вы отправили некорректное сообщение, попробуйте еще раз";

    private final TelegramBot bot;

    private final MessageProcessor messageProcessor;

    @Autowired
    public MessageUpdatesListener(TelegramBot bot, MessageProcessor messageProcessor) {
        bot.setUpdatesListener(this);
        this.bot = bot;
        this.messageProcessor = messageProcessor;
    }

    @Override
    public int process(List<Update> updates) {
        updates.stream()
            .map(Update::message)
            .forEach(m -> {
                System.out.println(m.text());
                try {
                    bot.execute(new SendMessage(m.chat().id(), messageProcessor.createReply(m)));
                } catch (InvalidCommandException e) {
                    log.error("Error occur while message handling: ", e);
                    bot.execute(new SendMessage(m.chat().id(), INVALID_MESSAGE));
                }
            });
        return CONFIRMED_UPDATES_ALL;
    }
}

package backend.academy.bot.service.notifications;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.model.LinkUpdate;

@Service
public class BotNotificationManager {

    @Autowired
    private TelegramBot bot;

    public void notify(LinkUpdate update) {
        update.tgChatIds().forEach(id -> {
            bot.execute(new SendMessage(
                    id, "Произошли обновления по ссылке: ".concat(update.url().toString())));
        });
    }
}

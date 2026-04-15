package backend.academy.bot.service.notifications;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.model.LinkUpdate;

@Service
@Slf4j
public class BotNotificationManager {

    public void notify(LinkUpdate update) {
        //todo сделать отправку сообщения к приложению
        log.info(
                "Telegram integration is removed. Skipping notification dispatch for url={} and chatIds={}",
                update.url(),
                update.tgChatIds());
    }
}

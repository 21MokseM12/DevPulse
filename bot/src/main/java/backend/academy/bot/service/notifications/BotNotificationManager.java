package backend.academy.bot.service.notifications;

import backend.academy.bot.db.model.Notification;
import backend.academy.bot.db.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.model.LinkUpdate;

@Service
@Slf4j
@RequiredArgsConstructor
public class BotNotificationManager {

    private final NotificationRepository notificationRepository;

    public void notify(LinkUpdate update) {
        notificationRepository.save(new Notification(
                update.id(),
                update.url().toString(),
                update.title(),
                update.updateOwner(),
                update.description(),
                update.creationDate(),
                update.clientsIds()));
        log.info(
                "Saved incoming notification for url={} and clients={}",
                update.url(),
                update.clientsIds());
    }
}

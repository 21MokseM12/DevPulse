package backend.academy.bot.service.notifications;

import backend.academy.bot.db.repository.NotificationRepository;
import backend.academy.bot.mapper.LinkUpdateNotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.model.LinkUpdate;

@Service
@Slf4j
@RequiredArgsConstructor
public class BotNotificationManager {

    private final NotificationRepository notificationRepository;
    private final LinkUpdateNotificationMapper notificationMapper;

    public void notify(LinkUpdate update) {
        notificationRepository.save(notificationMapper.map(update));
        log.info(
                "Saved incoming notification for url={} and clients={}",
                update.url(),
                update.clientsIds());
    }
}

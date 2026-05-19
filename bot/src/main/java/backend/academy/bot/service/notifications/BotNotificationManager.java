package backend.academy.bot.service.notifications;

import backend.academy.bot.db.repository.NotificationRepository;
import backend.academy.bot.mapper.LinkUpdateNotificationMapper;
import backend.academy.bot.service.push.PushDispatchService;
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
    private final PushDispatchService pushDispatchService;

    public void notify(LinkUpdate update) {
        long notificationId = notificationRepository.save(notificationMapper.map(update));
        pushDispatchService.dispatchForUpdate(notificationId, update);
        log.info("Saved incoming notification for url={} and clients={}", update.url(), update.clientLogins());
    }
}

package backend.academy.scrapper.service.listeners;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.database.LinkService;
import backend.academy.scrapper.factory.LinkUpdaterServiceFactory;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.NotifyUpdateEntity;
import backend.academy.scrapper.service.notifications.impl.ScrapperHttpNotificationManager;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class LinkUpdateScheduledListener {

    private final LinkService linkService;

    private final ScrapperConfig scrapperConfig;

    private final LinkUpdaterServiceFactory updaterFactory;

    private final ScrapperHttpNotificationManager notificationManager;

    @Autowired
    public LinkUpdateScheduledListener(
            LinkService linkService,
            ScrapperConfig scrapperConfig,
            LinkUpdaterServiceFactory updaterFactory,
            ScrapperHttpNotificationManager notificationManager) {
        this.linkService = linkService;
        this.scrapperConfig = scrapperConfig;
        this.updaterFactory = updaterFactory;
        this.notificationManager = notificationManager;
    }

    @Scheduled(fixedDelayString = "#{ @scheduler.interval() }")
    public void listenUpdates() {
        Stream<URI> linkNeededCheck = linkService.findAllLinksByForceCheckDelay(
                scrapperConfig.scheduler().forceCheckDelay());
        List<NotifyUpdateEntity> notifyList = new ArrayList<>();

        linkNeededCheck.forEach(link -> {
            List<LinkUpdateDTO> response = updaterFactory.get(link).getUpdates(link);
            if (!response.isEmpty()) {
                List<Long> chatIdsNeededNotify = linkService.findSubscribedChats(link);
                notifyList.add(new NotifyUpdateEntity(link, response, chatIdsNeededNotify));
            }
        });
        notificationManager.notify(notifyList);
    }
}

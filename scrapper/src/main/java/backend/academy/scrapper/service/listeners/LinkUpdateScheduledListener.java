package backend.academy.scrapper.service.listeners;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.factory.LinkUpdaterServiceFactory;
import backend.academy.scrapper.database.model.Link;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.repository.ClientRepository;
import backend.academy.scrapper.service.notifications.impl.ScrapperHttpNotificationManager;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class LinkUpdateScheduledListener {

    private final ClientRepository clientRepository;

    private final ScrapperConfig scrapperConfig;

    private final LinkUpdaterServiceFactory updaterFactory;

    private final ScrapperHttpNotificationManager notificationManager;

    @Autowired
    public LinkUpdateScheduledListener(
            ClientRepository clientRepository,
            ScrapperConfig scrapperConfig,
            LinkUpdaterServiceFactory updaterFactory,
            ScrapperHttpNotificationManager notificationManager) {
        this.clientRepository = clientRepository;
        this.scrapperConfig = scrapperConfig;
        this.updaterFactory = updaterFactory;
        this.notificationManager = notificationManager;
    }

    @Scheduled(fixedDelayString = "#{ @scheduler.interval() }")
    // todo добавить проверку на уже отправленные изменения и не отправлять их по новой
    public void listenUpdates() {
        Map<Long, List<Link>> linkNeededCheck = clientRepository.findAllLinksByForceCheckDelay(
                scrapperConfig.scheduler().forceCheckDelay());

        Map<URI, List<Long>> linkWasUpdated = new HashMap<>();
        for (Map.Entry<Long, List<Link>> entry : linkNeededCheck.entrySet()) {
            for (Link link : entry.getValue()) {
                Optional<List<LinkUpdateDTO>> response =
                        updaterFactory.get(link.url()).getUpdates(link.url());
                if (response.isPresent()) {
                    if (!linkWasUpdated.containsKey(link.url())) {
                        linkWasUpdated.put(link.url(), new ArrayList<>());
                    }
                    linkWasUpdated.get(link.url()).add(entry.getKey());
                }
            }
        }
        if (!linkWasUpdated.isEmpty()) {
            notificationManager.notify(linkWasUpdated);
        }
    }
}

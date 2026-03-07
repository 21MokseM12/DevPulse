package backend.academy.scrapper.service.listeners;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.properties.DatabaseProperty;
import backend.academy.scrapper.factory.LinkUpdaterServiceFactory;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.NotifyUpdateEntity;
import backend.academy.scrapper.service.LinkOperationProcessor;
import backend.academy.scrapper.service.notifications.impl.ScrapperHttpNotificationManager;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class LinkUpdateScheduledListener {

    private final ScrapperConfig scrapperConfig;
    private final DatabaseProperty databaseProperty;
    private final LinkUpdaterServiceFactory updaterFactory;
    private final LinkOperationProcessor linkOperationProcessor;
    private final ScrapperHttpNotificationManager notificationManager;

    private ExecutorService executor;

    @PostConstruct
    public void init() {
        this.executor = Executors.newFixedThreadPool(scrapperConfig.scheduler().threadPoolSize());
    }

    @Scheduled(fixedDelayString = "#{ @scheduler.interval() }")
    public void listenUpdates() {
        Set<URI> batch;
        int pageNum = 0,
                batchSize =
                        databaseProperty.pageSize() / scrapperConfig.scheduler().threadPoolSize();

        do {
            batch = linkOperationProcessor.findAllLinksByForceCheckDelay(
                    scrapperConfig.scheduler().forceCheckDelay(), pageNum);
            List<CompletableFuture<List<NotifyUpdateEntity>>> futures = new ArrayList<>();
            ListUtils.partition(new ArrayList<>(batch), batchSize).forEach(part -> {
                futures.add(CompletableFuture.supplyAsync(() -> processLink(part), executor)
                        .completeOnTimeout(Collections.emptyList(), 10, TimeUnit.SECONDS)
                        .exceptionally(ex -> {
                            log.error("Error processing batch", ex);
                            return Collections.emptyList();
                        }));
            });

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            List<NotifyUpdateEntity> notifyList = allFutures
                    .thenApply(v -> futures.stream()
                            .flatMap(future -> future.join().stream())
                            .collect(Collectors.toList()))
                    .join();
            notificationManager.notify(notifyList);

            pageNum++;
        } while (!batch.isEmpty());
    }

    private List<NotifyUpdateEntity> processLink(List<URI> links) {
        List<NotifyUpdateEntity> notifyList = new ArrayList<>();
        links.forEach(link -> {
            List<LinkUpdateDTO> response = updaterFactory.get(link).getUpdates(link);
            if (!response.isEmpty()) {
                List<Long> chatIdsNeededNotify = linkOperationProcessor.findSubscribedChats(link);
                notifyList.add(new NotifyUpdateEntity(link, response, chatIdsNeededNotify));
            }
        });
        return notifyList;
    }
}

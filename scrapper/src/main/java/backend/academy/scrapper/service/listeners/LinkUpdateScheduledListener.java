package backend.academy.scrapper.service.listeners;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.properties.DatabaseProperty;
import backend.academy.scrapper.enums.ProcessedIdType;
import backend.academy.scrapper.factory.LinkUpdaterServiceFactory;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.NotifyUpdateEntity;
import backend.academy.scrapper.model.UpdateType;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import backend.academy.scrapper.service.LinkOperationProcessor;
import backend.academy.scrapper.service.notifications.NotificationManager;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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

    private static final long LINK_BATCH_TIMEOUT_SECONDS = 60L;

    private final ScrapperConfig scrapperConfig;
    private final DatabaseProperty databaseProperty;
    private final LinkUpdaterServiceFactory updaterFactory;
    private final LinkOperationProcessor linkOperationProcessor;
    private final NotificationManager notificationManager;

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
                        .exceptionally(ex -> {
                            Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                            if (cause instanceof TimeoutException) {
                                log.warn(
                                        "Timeout processing links batch ({} item(s)) after {}s, retrying on next scheduler tick",
                                        part.size(),
                                        LINK_BATCH_TIMEOUT_SECONDS);
                            } else {
                                log.error("Error processing batch", ex);
                            }
                            return Collections.emptyList();
                        }));
            });

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .orTimeout(LINK_BATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                        if (cause instanceof TimeoutException) {
                            log.warn(
                                    "Global links polling timeout after {}s; partial results may be deferred to next tick",
                                    LINK_BATCH_TIMEOUT_SECONDS);
                        } else {
                            log.error("Error waiting for links polling batch", ex);
                        }
                        return null;
                    });

            List<NotifyUpdateEntity> notifyList = allFutures
                    .thenApply(v -> futures.stream()
                            .flatMap(future -> future.join().stream())
                            .collect(Collectors.toList()))
                    .join();
            if (!notifyList.isEmpty()) {
                List<NotifyUpdateEntity> deliveredNotifications = Optional.ofNullable(
                                notificationManager.notify(notifyList))
                        .orElseGet(List::of);
                markUpdatesAsProcessed(deliveredNotifications);
            }

            pageNum++;
        } while (!batch.isEmpty());
    }

    private List<NotifyUpdateEntity> processLink(List<URI> links) {
        List<NotifyUpdateEntity> notifyList = new ArrayList<>();
        links.forEach(link -> {
            OffsetDateTime checkedAt = OffsetDateTime.now();
            try {
                List<LinkUpdateDTO> response = updaterFactory.get(link).getUpdates(link);
                linkOperationProcessor.markPollingSuccess(
                        link, checkedAt, scrapperConfig.scheduler().forceCheckDelay());
                response.forEach(update -> {
                    List<Long> chatIdsNeededNotify = linkOperationProcessor.findSubscribedChats(link, update);
                    if (!chatIdsNeededNotify.isEmpty()) {
                        List<String> clientLogins = linkOperationProcessor.findClientLogins(chatIdsNeededNotify);
                        if (!clientLogins.isEmpty()) {
                            notifyList.add(new NotifyUpdateEntity(link, List.of(update), clientLogins));
                        }
                    }
                });
            } catch (Exception ex) {
                String failureMessage = ex.getClass().getSimpleName() + ": " + ex.getMessage();
                log.warn("Ошибка опроса ссылки {}: {}", link, failureMessage, ex);
                linkOperationProcessor.markPollingFailure(
                        link, checkedAt, scrapperConfig.scheduler().forceCheckDelay(), failureMessage);
            }
        });
        return notifyList;
    }

    private void markUpdatesAsProcessed(List<NotifyUpdateEntity> deliveredNotifications) {
        deliveredNotifications.forEach(notification -> {
            List<ProcessedIdDTO> processedIds = notification.updates().stream()
                    .map(this::toProcessedId)
                    .flatMap(Optional::stream)
                    .toList();
            if (!processedIds.isEmpty()) {
                linkOperationProcessor.saveProcessedIds(notification.link(), processedIds);
            }
        });
    }

    private Optional<ProcessedIdDTO> toProcessedId(LinkUpdateDTO update) {
        if (update.type() == null || update.id() == null) {
            return Optional.empty();
        }
        return Optional.of(new ProcessedIdDTO(update.id(), mapProcessedIdType(update.type())));
    }

    private ProcessedIdType mapProcessedIdType(UpdateType type) {
        return switch (type) {
            case GITHUB_ISSUE -> ProcessedIdType.GITHUB_ISSUE;
            case GITHUB_PULL_REQUEST -> ProcessedIdType.GITHUB_PULL_REQUEST;
            case GITHUB_COMMIT -> ProcessedIdType.GITHUB_COMMIT;
            case STACKOVERFLOW_ANSWER -> ProcessedIdType.STACKOVERFLOW_ANSWER;
            case STACKOVERFLOW_COMMENT -> ProcessedIdType.STACKOVERFLOW_COMMENT;
        };
    }
}

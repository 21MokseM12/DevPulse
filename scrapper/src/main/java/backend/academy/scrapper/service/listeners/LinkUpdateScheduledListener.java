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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
        String cycleId = UUID.randomUUID().toString();
        long cycleStartNanos = System.nanoTime();
        PollingCycleStats stats = new PollingCycleStats();
        Set<URI> batch;
        int pageNum = 0,
                batchSize =
                        databaseProperty.pageSize() / scrapperConfig.scheduler().threadPoolSize();
        log.info(
                "Начинается цикл опроса ссылок (cycleId={}, interval={}, forceCheckDelay={}, размер_пакета={})",
                cycleId,
                scrapperConfig.scheduler().interval(),
                scrapperConfig.scheduler().forceCheckDelay(),
                batchSize);

        do {
            batch = linkOperationProcessor.findAllLinksByForceCheckDelay(
                    scrapperConfig.scheduler().forceCheckDelay(), pageNum);
            stats.totalLinks += batch.size();
            log.info(
                    "Получен пакет ссылок для опроса: {} шт. (cycleId={}, страница={})",
                    batch.size(),
                    cycleId,
                    pageNum);
            List<CompletableFuture<LinkBatchProcessingResult>> futures = new ArrayList<>();
            ListUtils.partition(new ArrayList<>(batch), batchSize).forEach(part -> {
                futures.add(CompletableFuture.supplyAsync(() -> processLink(part, cycleId), executor)
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
                            return LinkBatchProcessingResult.empty();
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

            LinkBatchProcessingResult batchResult = allFutures
                    .thenApply(v -> futures.stream()
                            .map(CompletableFuture::join)
                            .reduce(LinkBatchProcessingResult.empty(), LinkBatchProcessingResult::merge))
                    .join();
            stats.successLinks += batchResult.successCount();
            stats.failedLinks += batchResult.failedCount();
            stats.noUpdatesLinks += batchResult.noUpdatesCount();
            stats.updatesFound += batchResult.updatesFound();

            if (!batchResult.notifications().isEmpty()) {
                int plannedNotifications = batchResult.notifications().stream()
                        .mapToInt(notification -> notification.updates().size())
                        .sum();
                log.info(
                        "Начинается отправка уведомлений (cycleId={}, ссылок={}, обновлений={})",
                        cycleId,
                        batchResult.notifications().size(),
                        plannedNotifications);
                List<NotifyUpdateEntity> deliveredNotifications = Optional.ofNullable(
                                notificationManager.notify(batchResult.notifications()))
                        .orElseGet(List::of);
                markUpdatesAsProcessed(deliveredNotifications);
                int deliveredUpdates = deliveredNotifications.stream()
                        .mapToInt(notification -> notification.updates().size())
                        .sum();
                stats.notificationsSent += deliveredUpdates;
                log.info("Уведомления отправлены (cycleId={}, доставлено_обновлений={})", cycleId, deliveredUpdates);
            }

            pageNum++;
        } while (!batch.isEmpty());
        long cycleDurationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - cycleStartNanos);
        log.info(
                "Цикл опроса завершен: всего={}, успешно={}, без_изменений={}, с_ошибкой={}, новых_событий={}, уведомлений_отправлено={}, длительность={}мс (cycleId={})",
                stats.totalLinks,
                stats.successLinks,
                stats.noUpdatesLinks,
                stats.failedLinks,
                stats.updatesFound,
                stats.notificationsSent,
                cycleDurationMs,
                cycleId);
    }

    private LinkBatchProcessingResult processLink(List<URI> links, String cycleId) {
        List<NotifyUpdateEntity> notifyList = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;
        int noUpdatesCount = 0;
        int updatesFound = 0;
        for (URI link : links) {
            long linkStartNanos = System.nanoTime();
            String result = "success";
            OffsetDateTime checkedAt = OffsetDateTime.now();
            try {
                log.info("Начинается просмотр по ссылке: {} (cycleId={})", link, cycleId);
                var updater = updaterFactory.get(link);
                String provider = updater.getType().name();
                List<LinkUpdateDTO> response = updater.getUpdates(link);
                linkOperationProcessor.markPollingSuccess(
                        link, checkedAt, scrapperConfig.scheduler().forceCheckDelay());
                log.info("Ответ от {} получен успешно по ссылке: {} (cycleId={})", provider, link, cycleId);
                log.info("Просмотр успешен - все события получены по ссылке: {} (cycleId={})", link, cycleId);
                if (response.isEmpty()) {
                    noUpdatesCount++;
                    result = "no_updates";
                    log.info("Новых событий не обнаружено по ссылке: {} (cycleId={})", link, cycleId);
                } else {
                    updatesFound += response.size();
                    log.info("Найдены новые события: {} по ссылке {} (cycleId={})", response.size(), link, cycleId);
                }
                response.forEach(update -> {
                    List<Long> chatIdsNeededNotify = linkOperationProcessor.findSubscribedChats(link, update);
                    if (!chatIdsNeededNotify.isEmpty()) {
                        List<String> clientLogins = linkOperationProcessor.findClientLogins(chatIdsNeededNotify);
                        if (!clientLogins.isEmpty()) {
                            notifyList.add(new NotifyUpdateEntity(link, List.of(update), clientLogins));
                        }
                    }
                });
                successCount++;
            } catch (Exception ex) {
                String failureMessage = ex.getClass().getSimpleName() + ": " + ex.getMessage();
                result = "failed";
                failedCount++;
                log.warn(
                        "Просмотр по ссылке завершился с ошибкой: {}. Причина: {} (cycleId={})",
                        link,
                        failureMessage,
                        cycleId,
                        ex);
                linkOperationProcessor.markPollingFailure(
                        link, checkedAt, scrapperConfig.scheduler().forceCheckDelay(), failureMessage);
            } finally {
                long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - linkStartNanos);
                log.info(
                        "Просмотр завершен по ссылке: {} (результат={}, длительность={}мс, cycleId={})",
                        link,
                        result,
                        durationMs,
                        cycleId);
            }
        }
        return new LinkBatchProcessingResult(notifyList, successCount, failedCount, noUpdatesCount, updatesFound);
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

    private static final class PollingCycleStats {
        private int totalLinks;
        private int successLinks;
        private int failedLinks;
        private int noUpdatesLinks;
        private int updatesFound;
        private int notificationsSent;
    }

    private record LinkBatchProcessingResult(
            List<NotifyUpdateEntity> notifications,
            int successCount,
            int failedCount,
            int noUpdatesCount,
            int updatesFound) {

        private static LinkBatchProcessingResult empty() {
            return new LinkBatchProcessingResult(Collections.emptyList(), 0, 0, 0, 0);
        }

        private LinkBatchProcessingResult merge(LinkBatchProcessingResult other) {
            List<NotifyUpdateEntity> mergedNotifications = new ArrayList<>(notifications);
            mergedNotifications.addAll(other.notifications);
            return new LinkBatchProcessingResult(
                    mergedNotifications,
                    successCount + other.successCount,
                    failedCount + other.failedCount,
                    noUpdatesCount + other.noUpdatesCount,
                    updatesFound + other.updatesFound);
        }
    }
}

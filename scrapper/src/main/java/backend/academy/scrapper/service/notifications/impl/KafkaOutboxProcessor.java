package backend.academy.scrapper.service.notifications.impl;

import backend.academy.scrapper.config.CommonKafkaConfig;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.ScrapperConfig.DeliveryMode;
import backend.academy.scrapper.config.properties.CommonKafkaProperties;
import backend.academy.scrapper.db.model.KafkaOutboxMessage;
import backend.academy.scrapper.db.repository.KafkaOutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.model.LinkUpdate;

@Slf4j
@Service
public class KafkaOutboxProcessor {

    private final KafkaOutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final ScrapperConfig scrapperConfig;
    private final CommonKafkaProperties kafkaProperties;
    private final Clock clock;
    private final Counter successCounter;
    private final Counter failureCounter;
    private final AtomicLong pendingGauge;

    public KafkaOutboxProcessor(
            KafkaOutboxRepository outboxRepository,
            @Qualifier(CommonKafkaConfig.COMMON_KAFKA_TEMPLATE) KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper objectMapper,
            ScrapperConfig scrapperConfig,
            CommonKafkaProperties kafkaProperties,
            Clock clock,
            MeterRegistry meterRegistry) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.scrapperConfig = scrapperConfig;
        this.kafkaProperties = kafkaProperties;
        this.clock = clock;
        this.successCounter = meterRegistry.counter("scrapper.outbox.published.success");
        this.failureCounter = meterRegistry.counter("scrapper.outbox.published.failure");
        this.pendingGauge = meterRegistry.gauge("scrapper.outbox.pending.count", new AtomicLong(0));
    }

    @Scheduled(fixedDelayString = "#{@outbox.interval()}")
    public void processPendingMessages() {
        processBatch();
    }

    public void processBatch() {
        if (scrapperConfig.delivery().mode() != DeliveryMode.KAFKA) {
            return;
        }
        List<KafkaOutboxMessage> batch =
                outboxRepository.findPendingBatch(scrapperConfig.outbox().batchSize());
        if (batch.isEmpty()) {
            updatePendingGauge();
            return;
        }

        log.info("Outbox processor fetched {} message(s)", batch.size());
        for (KafkaOutboxMessage message : batch) {
            processMessage(message);
        }
        updatePendingGauge();
    }

    private void processMessage(KafkaOutboxMessage message) {
        try {
            LinkUpdate linkUpdate = objectMapper.readValue(message.payload(), LinkUpdate.class);
            PublishResult publishResult = publishWithRetry(message.topic(), linkUpdate);
            if (publishResult.successful()) {
                outboxRepository.markSent(message.id(), currentUtcTime(), publishResult.attempts());
                successCounter.increment();
                return;
            }
            outboxRepository.incrementAttemptCount(message.id(), publishResult.attempts());
            failureCounter.increment();
            log.error("Failed to publish outbox id={} topic={}", message.id(), message.topic(), publishResult.error());
        } catch (Exception e) {
            outboxRepository.incrementAttemptCount(message.id(), 1);
            failureCounter.increment();
            log.error("Failed to process outbox id={} topic={}", message.id(), message.topic(), e);
        }
    }

    private PublishResult publishWithRetry(String topic, LinkUpdate payload) {
        CommonKafkaProperties.RetryPolicyProperties retryPolicy = kafkaProperties.retryPolicy();
        int maxAttempts = Math.max(1, retryPolicy.maxAttempts());
        long delay = Math.max(1L, retryPolicy.interval());
        int attempts = 0;
        Exception lastError = null;

        while (attempts < maxAttempts) {
            attempts++;
            try {
                long sendTimeout = Math.max(1_000L, retryPolicy.maxDelay());
                kafkaTemplate.send(topic, payload).get(sendTimeout, TimeUnit.MILLISECONDS);
                return new PublishResult(true, attempts, null);
            } catch (Exception e) {
                lastError = e;
                if (attempts >= maxAttempts) {
                    break;
                }
                try {
                    sleep(delay);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    return new PublishResult(false, attempts, interruptedException);
                }
                delay = Math.min(
                        (long) (delay * Math.max(1.0, retryPolicy.multiplier())), Math.max(1L, retryPolicy.maxDelay()));
            }
        }
        return new PublishResult(false, attempts, lastError);
    }

    private void sleep(long delay) throws InterruptedException {
        Thread.sleep(delay);
    }

    private LocalDateTime currentUtcTime() {
        return OffsetDateTime.now(clock).toLocalDateTime();
    }

    private void updatePendingGauge() {
        if (pendingGauge != null) {
            pendingGauge.set(outboxRepository.countPending());
        }
    }

    private record PublishResult(boolean successful, int attempts, Exception error) {}
}

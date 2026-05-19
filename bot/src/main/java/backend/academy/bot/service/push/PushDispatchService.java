package backend.academy.bot.service.push;

import backend.academy.bot.config.properties.PushProperties;
import backend.academy.bot.db.model.PushToken;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.model.LinkUpdate;

@Slf4j
@Service
public class PushDispatchService {
    private final PushTokenService pushTokenService;
    private final PushSender pushSender;
    private final PushPayloadBuilder payloadBuilder;
    private final PushProperties pushProperties;
    private final MeterRegistry meterRegistry;
    private final Map<String, Counter> failureCounters = new ConcurrentHashMap<>();

    private final Counter attempts;
    private final Counter success;
    private final Counter invalidTokenCounter;
    private final Counter jobFailureCounter;
    private final Timer latency;
    private final DistributionSummary activeTokensPerUser;

    private final AtomicLong attemptCount = new AtomicLong();
    private final AtomicLong invalidCount = new AtomicLong();

    public PushDispatchService(
            PushTokenService pushTokenService,
            PushSender pushSender,
            PushPayloadBuilder payloadBuilder,
            PushProperties pushProperties,
            MeterRegistry meterRegistry) {
        this.pushTokenService = pushTokenService;
        this.pushSender = pushSender;
        this.payloadBuilder = payloadBuilder;
        this.pushProperties = pushProperties;
        this.meterRegistry = meterRegistry;
        this.attempts = Counter.builder("push_send_attempt_total").register(meterRegistry);
        this.success = Counter.builder("push_send_success_total").register(meterRegistry);
        this.invalidTokenCounter = Counter.builder("push_invalid_token_total").register(meterRegistry);
        this.jobFailureCounter =
                Counter.builder("push_job_processing_failure_total").register(meterRegistry);
        this.latency = Timer.builder("push_send_latency").register(meterRegistry);
        this.activeTokensPerUser =
                DistributionSummary.builder("push_active_tokens_per_user").register(meterRegistry);
        Gauge.builder("push_invalid_token_rate", this, PushDispatchService::invalidRate)
                .register(meterRegistry);
    }

    @Async("pushTaskExecutor")
    public void dispatchForUpdate(long eventId, LinkUpdate update) {
        PushMessagePayload payload = payloadBuilder.build(eventId, update);
        for (String clientLogin : new LinkedHashSet<>(update.clientLogins())) {
            if (clientLogin == null || clientLogin.isBlank()) {
                continue;
            }
            try {
                dispatchToClient(clientLogin, payload);
            } catch (RuntimeException ex) {
                jobFailureCounter.increment();
                log.error("Push dispatch job failed for login={}", clientLogin, ex);
            }
        }
    }

    private void dispatchToClient(String clientLogin, PushMessagePayload payload) {
        var activeTokens = pushTokenService.findActiveByClientLogin(clientLogin);
        activeTokensPerUser.record(activeTokens.size());
        if (activeTokens.isEmpty()) {
            return;
        }
        for (PushToken token : activeTokens) {
            deliverWithRetry(token, payload);
        }
    }

    private void deliverWithRetry(PushToken token, PushMessagePayload payload) {
        long delayMs = pushProperties.retry().initialDelayMs();
        String lastFailureReason = "unknown";
        for (int attempt = 1; attempt <= pushProperties.retry().maxAttempts(); attempt++) {
            attempts.increment();
            attemptCount.incrementAndGet();
            Timer.Sample sample = Timer.start();
            PushDeliveryResult result = pushSender.send(token.token(), payload);
            sample.stop(latency);
            if (result.status() == PushDeliveryStatus.SUCCESS) {
                success.increment();
                return;
            }
            if (result.status() == PushDeliveryStatus.INVALID_TOKEN) {
                invalidTokenCounter.increment();
                invalidCount.incrementAndGet();
                pushTokenService.markInvalid(token);
                failure(result.reason());
                return;
            }
            if (result.status() == PushDeliveryStatus.TRANSIENT_ERROR
                    && attempt < pushProperties.retry().maxAttempts()) {
                sleep(delayMs);
                delayMs = Math.min(
                        (long) (delayMs * pushProperties.retry().multiplier()),
                        pushProperties.retry().maxDelayMs());
                continue;
            }
            lastFailureReason = result.reason();
            attempt = pushProperties.retry().maxAttempts();
        }
        failure(lastFailureReason);
    }

    private void failure(String reason) {
        String reasonTag = reason == null ? "unknown" : reason;
        failureCounters
                .computeIfAbsent(reasonTag, key -> Counter.builder("push_send_failure_total")
                        .tag("reason", key)
                        .register(meterRegistry))
                .increment();
    }

    private void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Push retry sleep interrupted", ex);
        }
    }

    private double invalidRate() {
        long attemptsValue = attemptCount.get();
        if (attemptsValue == 0L) {
            return 0D;
        }
        return (double) invalidCount.get() / (double) attemptsValue;
    }
}

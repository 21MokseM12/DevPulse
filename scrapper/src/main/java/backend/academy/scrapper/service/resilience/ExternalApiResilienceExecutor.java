package backend.academy.scrapper.service.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalApiResilienceExecutor {

    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final Set<String> instrumentedProfiles = ConcurrentHashMap.newKeySet();

    public <T> T execute(String profileName, Supplier<T> operation) {
        Retry retry = retryRegistry.retry(profileName);
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(profileName);
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(profileName);
        instrumentResilienceLogging(profileName, retry, circuitBreaker, rateLimiter);

        Supplier<T> decoratedOperation = Retry.decorateSupplier(retry, operation);
        decoratedOperation = CircuitBreaker.decorateSupplier(circuitBreaker, decoratedOperation);
        decoratedOperation = RateLimiter.decorateSupplier(rateLimiter, decoratedOperation);
        return decoratedOperation.get();
    }

    private void instrumentResilienceLogging(
            String profileName, Retry retry, CircuitBreaker circuitBreaker, RateLimiter rateLimiter) {
        if (!instrumentedProfiles.add(profileName)) {
            return;
        }
        retry.getEventPublisher()
                .onRetry(event -> log.warn(
                        "Retry {} для профиля {} из-за {}: {}",
                        event.getNumberOfRetryAttempts(),
                        profileName,
                        event.getLastThrowable() == null
                                ? "unknown"
                                : event.getLastThrowable().getClass().getName(),
                        event.getLastThrowable() == null
                                ? "unknown"
                                : event.getLastThrowable().getMessage(),
                        event.getLastThrowable()))
                .onError(event -> log.error(
                        "Retry исчерпан для профиля {}. Последняя ошибка {}: {}",
                        profileName,
                        event.getLastThrowable() == null
                                ? "unknown"
                                : event.getLastThrowable().getClass().getName(),
                        event.getLastThrowable() == null
                                ? "unknown"
                                : event.getLastThrowable().getMessage(),
                        event.getLastThrowable()));
        circuitBreaker
                .getEventPublisher()
                .onError(event -> log.error(
                        "CircuitBreaker ошибка профиля {}: duration={}ms, throwable={}",
                        profileName,
                        event.getElapsedDuration().toMillis(),
                        event.getThrowable().getClass().getName(),
                        event.getThrowable()))
                .onStateTransition(event -> log.warn(
                        "CircuitBreaker transition профиля {}: {} -> {}",
                        profileName,
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()));
        rateLimiter
                .getEventPublisher()
                .onFailure(event -> log.warn("RateLimiter отказал профилю {}: {}", profileName, event));
    }
}

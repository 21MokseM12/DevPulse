package backend.academy.scrapper.service.resilience;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

class ExternalApiResilienceExecutorTest {

    @Test
    void execute_whenFailureIsRetriable_retriesAndReturnsResult() {
        ExternalApiResilienceExecutor executor = newExecutorWith(
                RetryConfig.custom()
                        .maxAttempts(3)
                        .retryExceptions(IllegalStateException.class)
                        .build(),
                CircuitBreakerConfig.ofDefaults(),
                RateLimiterConfig.custom()
                        .limitForPeriod(10)
                        .limitRefreshPeriod(Duration.ofSeconds(1))
                        .timeoutDuration(Duration.ZERO)
                        .build());
        AtomicInteger attempts = new AtomicInteger();

        String result = executor.execute("test-api", () -> {
            int currentAttempt = attempts.incrementAndGet();
            if (currentAttempt < 3) {
                throw new IllegalStateException("temporary");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(3, attempts.get());
    }

    @Test
    void execute_whenFailureIsNetworkRelated_retriesAndReturnsResult() {
        ExternalApiResilienceExecutor executor = newExecutorWith(
                RetryConfig.custom()
                        .maxAttempts(3)
                        .retryExceptions(ResourceAccessException.class)
                        .build(),
                CircuitBreakerConfig.ofDefaults(),
                RateLimiterConfig.custom()
                        .limitForPeriod(10)
                        .limitRefreshPeriod(Duration.ofSeconds(1))
                        .timeoutDuration(Duration.ZERO)
                        .build());
        AtomicInteger attempts = new AtomicInteger();

        String result = executor.execute("test-api", () -> {
            int currentAttempt = attempts.incrementAndGet();
            if (currentAttempt == 1) {
                throw new ResourceAccessException("connection closed before response");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(2, attempts.get());
    }

    @Test
    void execute_whenFailureIsNotRetriable_doesNotRetry() {
        ExternalApiResilienceExecutor executor = newExecutorWith(
                RetryConfig.custom()
                        .maxAttempts(3)
                        .retryExceptions(IllegalStateException.class)
                        .build(),
                CircuitBreakerConfig.ofDefaults(),
                RateLimiterConfig.custom()
                        .limitForPeriod(10)
                        .limitRefreshPeriod(Duration.ofSeconds(1))
                        .timeoutDuration(Duration.ZERO)
                        .build());
        AtomicInteger attempts = new AtomicInteger();

        assertThrows(
                IllegalArgumentException.class,
                () -> executor.execute("test-api", () -> {
                    attempts.incrementAndGet();
                    throw new IllegalArgumentException("bad request");
                }));

        assertEquals(1, attempts.get());
    }

    @Test
    void execute_whenCircuitBreakerOpened_rejectsNextCall() {
        ExternalApiResilienceExecutor executor = newExecutorWith(
                RetryConfig.custom()
                        .maxAttempts(1)
                        .retryExceptions(IllegalStateException.class)
                        .build(),
                CircuitBreakerConfig.custom()
                        .slidingWindowSize(2)
                        .minimumNumberOfCalls(2)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(5))
                        .build(),
                RateLimiterConfig.custom()
                        .limitForPeriod(10)
                        .limitRefreshPeriod(Duration.ofSeconds(1))
                        .timeoutDuration(Duration.ZERO)
                        .build());
        AtomicInteger attempts = new AtomicInteger();

        assertThrows(
                IllegalStateException.class,
                () -> executor.execute("test-api", () -> {
                    attempts.incrementAndGet();
                    throw new IllegalStateException("downstream unavailable");
                }));
        assertThrows(
                IllegalStateException.class,
                () -> executor.execute("test-api", () -> {
                    attempts.incrementAndGet();
                    throw new IllegalStateException("downstream unavailable");
                }));

        assertThrows(
                CallNotPermittedException.class,
                () -> executor.execute("test-api", () -> {
                    attempts.incrementAndGet();
                    return "should not be called";
                }));
        assertEquals(2, attempts.get());
    }

    @Test
    void execute_whenRateLimitExceeded_rejectsRequest() {
        ExternalApiResilienceExecutor executor = newExecutorWith(
                RetryConfig.ofDefaults(),
                CircuitBreakerConfig.ofDefaults(),
                RateLimiterConfig.custom()
                        .limitForPeriod(1)
                        .limitRefreshPeriod(Duration.ofSeconds(30))
                        .timeoutDuration(Duration.ZERO)
                        .build());

        assertEquals("ok", executor.execute("test-api", () -> "ok"));
        assertThrows(RequestNotPermitted.class, () -> executor.execute("test-api", () -> "second"));
    }

    private ExternalApiResilienceExecutor newExecutorWith(
            RetryConfig retryConfig, CircuitBreakerConfig circuitBreakerConfig, RateLimiterConfig rateLimiterConfig) {
        RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
        RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(rateLimiterConfig);
        return new ExternalApiResilienceExecutor(retryRegistry, circuitBreakerRegistry, rateLimiterRegistry);
    }
}

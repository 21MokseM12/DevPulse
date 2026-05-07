package backend.academy.scrapper.service.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExternalApiResilienceExecutor {

    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;

    public <T> T execute(String profileName, Supplier<T> operation) {
        Retry retry = retryRegistry.retry(profileName);
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(profileName);
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(profileName);

        Supplier<T> decoratedOperation = Retry.decorateSupplier(retry, operation);
        decoratedOperation = CircuitBreaker.decorateSupplier(circuitBreaker, decoratedOperation);
        decoratedOperation = RateLimiter.decorateSupplier(rateLimiter, decoratedOperation);
        return decoratedOperation.get();
    }
}

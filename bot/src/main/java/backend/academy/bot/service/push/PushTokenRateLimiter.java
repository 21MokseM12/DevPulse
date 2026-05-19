package backend.academy.bot.service.push;

import backend.academy.bot.config.properties.PushProperties;
import backend.academy.bot.exceptions.RateLimitExceededException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.stereotype.Component;

@Component
public class PushTokenRateLimiter {
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Instant>> buckets = new ConcurrentHashMap<>();
    private final int limitPerMinute;

    public PushTokenRateLimiter(PushProperties pushProperties) {
        this.limitPerMinute = pushProperties.tokenApiRateLimitPerMinute();
    }

    public void check(String clientLogin) {
        Instant now = Instant.now();
        ConcurrentLinkedQueue<Instant> queue =
                buckets.computeIfAbsent(clientLogin, key -> new ConcurrentLinkedQueue<>());
        Instant threshold = now.minus(WINDOW);
        while (true) {
            Instant head = queue.peek();
            if (head == null || !head.isBefore(threshold)) {
                break;
            }
            queue.poll();
        }
        if (queue.size() >= limitPerMinute) {
            throw new RateLimitExceededException("Rate limit exceeded for push token operations");
        }
        queue.add(now);
    }
}

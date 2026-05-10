package backend.academy.scrapper.service.cache;

import backend.academy.scrapper.config.properties.RedisCacheProperty;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RedisPollingCacheService {

    private static final String ETAG_PREFIX = "etag:";
    private static final String LAST_MODIFIED_PREFIX = "poll:lastModified:";
    private static final String LAST_EVENT_DATE_PREFIX = "poll:lastEventDate:";
    private static final String LAST_CHECKED_AT_PREFIX = "poll:lastCheck:";

    private final StringRedisTemplate redisTemplate;
    private final RedisCacheProperty cacheProperty;
    private final Timer readTimer;
    private final Timer writeTimer;
    private final Counter hitCounter;
    private final Counter missCounter;
    private final Counter errorCounter;

    public RedisPollingCacheService(
            StringRedisTemplate redisTemplate, RedisCacheProperty cacheProperty, MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.cacheProperty = cacheProperty;
        this.readTimer = meterRegistry.timer("scrapper.redis.operation.latency", "operation", "read");
        this.writeTimer = meterRegistry.timer("scrapper.redis.operation.latency", "operation", "write");
        this.hitCounter = meterRegistry.counter("scrapper.redis.cache.hits");
        this.missCounter = meterRegistry.counter("scrapper.redis.cache.misses");
        this.errorCounter = meterRegistry.counter("scrapper.redis.cache.errors");
    }

    public Optional<String> getEtag(URI link) {
        return getValue(buildKey(ETAG_PREFIX, link));
    }

    public void saveEtag(URI link, String etag) {
        saveValue(buildKey(ETAG_PREFIX, link), etag, cacheProperty.etagTtl());
    }

    public Optional<OffsetDateTime> getLastModified(URI link) {
        return getValue(buildKey(LAST_MODIFIED_PREFIX, link)).map(OffsetDateTime::parse);
    }

    public void saveLastModified(URI link, OffsetDateTime value) {
        saveValue(buildKey(LAST_MODIFIED_PREFIX, link), value.toString(), cacheProperty.pollHintTtl());
    }

    public Optional<OffsetDateTime> getLastEventDate(URI link) {
        return getValue(buildKey(LAST_EVENT_DATE_PREFIX, link)).map(OffsetDateTime::parse);
    }

    public void saveLastEventDate(URI link, OffsetDateTime value) {
        saveValue(buildKey(LAST_EVENT_DATE_PREFIX, link), value.toString(), cacheProperty.pollHintTtl());
    }

    public void saveLastCheckedAt(URI link, OffsetDateTime value) {
        saveValue(buildKey(LAST_CHECKED_AT_PREFIX, link), value.toString(), cacheProperty.pollHintTtl());
    }

    private Optional<String> getValue(String key) {
        if (!cacheProperty.enabled()) {
            return Optional.empty();
        }
        try {
            return readTimer.record(() -> {
                String value = redisTemplate.opsForValue().get(key);
                if (value == null) {
                    missCounter.increment();
                    return Optional.empty();
                }
                hitCounter.increment();
                return Optional.of(value);
            });
        } catch (RuntimeException e) {
            errorCounter.increment();
            log.warn("Redis read failed for key {}: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    private void saveValue(String key, String value, java.time.Duration ttl) {
        if (!cacheProperty.enabled()) {
            return;
        }
        try {
            writeTimer.record(() -> redisTemplate.opsForValue().set(key, value, ttl));
        } catch (RuntimeException e) {
            errorCounter.increment();
            log.warn("Redis write failed for key {}: {}", key, e.getMessage());
        }
    }

    private String buildKey(String prefix, URI link) {
        return prefix + URLEncoder.encode(link.toString(), StandardCharsets.UTF_8);
    }
}

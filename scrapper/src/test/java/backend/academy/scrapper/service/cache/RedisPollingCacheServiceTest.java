package backend.academy.scrapper.service.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.config.properties.RedisCacheProperty;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisPollingCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisPollingCacheService redisPollingCacheService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RedisCacheProperty cacheProperty = new RedisCacheProperty(true, Duration.ofHours(1), Duration.ofMinutes(10));
        redisPollingCacheService =
                new RedisPollingCacheService(redisTemplate, cacheProperty, new SimpleMeterRegistry());
    }

    @Test
    void getEtag_whenValueExists_returnsCachedValue() {
        URI link = URI.create("https://github.com/example/repo");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn("\"etag-value\"");

        Optional<String> etag = redisPollingCacheService.getEtag(link);

        assertTrue(etag.isPresent());
        assertEquals("\"etag-value\"", etag.orElseThrow());
    }

    @Test
    void saveLastEventDate_whenRedisIsUnavailable_doesNotThrow() {
        URI link = URI.create("https://stackoverflow.com/questions/1/example");
        OffsetDateTime now = OffsetDateTime.now();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        org.mockito.Mockito.doThrow(new DataAccessResourceFailureException("redis down"))
                .when(valueOperations)
                .set(any(), any(), any(Duration.class));

        redisPollingCacheService.saveLastEventDate(link, now);

        verify(valueOperations).set(any(), eq(now.toString()), any(Duration.class));
    }

    @Test
    void getEtag_whenCacheDisabled_skipsRedisCalls() {
        RedisCacheProperty cacheProperty = new RedisCacheProperty(false, Duration.ofHours(1), Duration.ofMinutes(10));
        RedisPollingCacheService disabledCacheService =
                new RedisPollingCacheService(redisTemplate, cacheProperty, new SimpleMeterRegistry());

        Optional<String> etag = disabledCacheService.getEtag(URI.create("https://github.com/example/repo"));

        assertTrue(etag.isEmpty());
        verify(redisTemplate, never()).opsForValue();
    }
}

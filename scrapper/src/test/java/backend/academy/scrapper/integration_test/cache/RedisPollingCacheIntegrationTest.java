package backend.academy.scrapper.integration_test.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.scrapper.ScrapperApplication;
import backend.academy.scrapper.db.DbLinkService;
import backend.academy.scrapper.integration_test.config.TestContainersConfiguration;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import scrapper.bot.connectivity.model.request.AddLinkRequest;

@SpringBootTest(classes = {ScrapperApplication.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.cache.redis.enabled=true"})
class RedisPollingCacheIntegrationTest extends TestContainersConfiguration {

    @Autowired
    private DbLinkService dbLinkService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void etagCache_readThroughAndWriteThrough_usesRedisValue() {
        URI link = URI.create("https://github.com/devpulse/cache-it-" + System.nanoTime());
        dbLinkService.saveLink(new AddLinkRequest(link, Set.of("tag"), Set.of("filter")));
        dbLinkService.updateEtag(link, "\"cache-etag\"");

        jdbcTemplate.update("update links set etag = null where url = ?", link.toString());

        Optional<String> cachedEtag = dbLinkService.findEtagByLink(link);

        assertTrue(cachedEtag.isPresent());
        assertEquals("\"cache-etag\"", cachedEtag.orElseThrow());
    }

    @Test
    void pollHintCache_lastEventDate_returnsCachedValueWhenDbValueChanges() {
        URI link = URI.create("https://stackoverflow.com/questions/1/cache-it-" + System.nanoTime());
        dbLinkService.saveLink(new AddLinkRequest(link, Set.of("tag"), Set.of("filter")));
        OffsetDateTime cachedDate =
                OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS).minusMinutes(2);
        dbLinkService.updateLastEventDate(link, cachedDate);

        jdbcTemplate.update(
                """
                update poll_state ps
                set last_event_date = ?
                from links l
                where ps.link_id = l.id and l.url = ?
                """,
                java.sql.Timestamp.from(cachedDate.minusHours(1).toInstant()),
                link.toString());

        Optional<OffsetDateTime> lastEventDate = dbLinkService.findLastEventDateByLink(link);

        assertTrue(lastEventDate.isPresent());
        assertEquals(cachedDate.toInstant(), lastEventDate.orElseThrow().toInstant());
    }
}

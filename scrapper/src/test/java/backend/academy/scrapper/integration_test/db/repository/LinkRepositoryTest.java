package backend.academy.scrapper.integration_test.db.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.scrapper.config.ApplicationConfig;
import backend.academy.scrapper.db.model.Link;
import backend.academy.scrapper.db.repository.LinkRepository;
import backend.academy.scrapper.db.repository.impl.FilterRepositoryImpl;
import backend.academy.scrapper.db.repository.impl.LinkRepositoryImpl;
import backend.academy.scrapper.db.repository.impl.TagRepositoryImpl;
import backend.academy.scrapper.integration_test.config.TestContainersConfiguration;
import backend.academy.scrapper.mapper.LinkRowMapper;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@JdbcTest
@Testcontainers
@ActiveProfiles("test")
@Import({
    LinkRepositoryImpl.class,
    ApplicationConfig.class,
    FilterRepositoryImpl.class,
    TagRepositoryImpl.class,
    LinkRowMapper.class
})
public class LinkRepositoryTest extends TestContainersConfiguration {

    @Autowired
    private LinkRepository repository;

    @Autowired
    private Clock clock;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void save_thenFindById_returnsStoredLinkWithEmptyTagsAndFilters() {
        OffsetDateTime createdAt = OffsetDateTime.now(clock).minusMinutes(3);
        String url = "https://example.com/" + UUID.randomUUID();

        Long id = repository.save(url, createdAt);
        Optional<Link> found = repository.findById(id);
        Link foundLink = found.orElseThrow();

        assertNotNull(id);
        assertTrue(found.isPresent());
        assertEquals(id, foundLink.id());
        assertEquals(URI.create(url), foundLink.url());
        long deltaMicros = Math.abs(ChronoUnit.MICROS.between(
                createdAt.toInstant(), foundLink.createdAt().toInstant()));
        assertTrue(deltaMicros <= 1);
        assertEquals(Set.of(), foundLink.tags());
        assertEquals(Set.of(), foundLink.filters());
    }

    @Test
    public void findByLink_whenLinkDoesNotExist_thenThrowsEmptyResultDataAccessException() {
        String missingUrl = "https://missing.example/" + UUID.randomUUID();

        assertThrows(EmptyResultDataAccessException.class, () -> repository.findIdByLink(missingUrl));
    }

    @Test
    public void findById_whenIdDoesNotExist_thenThrowsEmptyResultDataAccessException() {
        assertThrows(EmptyResultDataAccessException.class, () -> repository.findById(Long.MAX_VALUE));
    }

    @Test
    public void existsLink_whenLinkIsNull_thenReturnsFalse() {
        assertFalse(repository.existsLink(null));
    }

    @Test
    public void delete_whenLinkExists_thenReturnsDeletedAndRemovesLink() {
        OffsetDateTime createdAt = OffsetDateTime.now(clock);
        String url = "https://to-delete.example/" + UUID.randomUUID();
        Long id = repository.save(url, createdAt);

        Optional<Link> deleted = repository.delete(id);
        Link deletedLink = deleted.orElseThrow();

        assertTrue(deleted.isPresent());
        assertEquals(id, deletedLink.id());
        assertEquals(URI.create(url), deletedLink.url());
        assertFalse(repository.existsLink(url));
    }

    @Test
    public void delete_whenLinkDoesNotExist_thenThrowsEmptyResultDataAccessException() {
        assertThrows(EmptyResultDataAccessException.class, () -> repository.delete(Long.MAX_VALUE));
    }

    @Test
    public void findAllLinksByUpdatedAt_whenUsingTimeBoundary_thenReturnsOnlyExpiredLinks() {
        OffsetDateTime now = OffsetDateTime.now(clock).minusMinutes(10);
        URI expiredUrl = URI.create("https://window.example/old-" + UUID.randomUUID());
        URI freshUrl = URI.create("https://window.example/new-" + UUID.randomUUID());

        repository.save(expiredUrl.toString(), now.minus(Duration.ofDays(1)));
        repository.save(freshUrl.toString(), now.plus(Duration.ofDays(1)));

        Set<URI> expiredLinks = repository.findAllLinksByUpdatedAt(now, 0, 10);

        assertTrue(expiredLinks.contains(expiredUrl));
        assertFalse(expiredLinks.contains(freshUrl));
    }

    @Test
    public void findAllLinksForPolling_whenBackoffActive_thenSkipsLink() {
        OffsetDateTime now = OffsetDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
        String readyUrl = "https://poll.example/ready-" + UUID.randomUUID();
        String delayedUrl = "https://poll.example/delayed-" + UUID.randomUUID();

        Long readyId = repository.save(readyUrl, now.minusMinutes(5));
        Long delayedId = repository.save(delayedUrl, now.minusMinutes(5));

        jdbcTemplate.update(
                "update poll_state set next_poll_at = ?, backoff_until = ? where link_id = ?",
                java.sql.Timestamp.from(now.minusSeconds(1).toInstant()),
                null,
                readyId);
        jdbcTemplate.update(
                "update poll_state set next_poll_at = ?, backoff_until = ? where link_id = ?",
                java.sql.Timestamp.from(now.minusSeconds(1).toInstant()),
                java.sql.Timestamp.from(now.plusMinutes(10).toInstant()),
                delayedId);

        Set<URI> dueLinks = repository.findAllLinksForPolling(now, 0, 20);

        assertTrue(dueLinks.contains(URI.create(readyUrl)));
        assertFalse(dueLinks.contains(URI.create(delayedUrl)));
    }

    @Test
    public void markPollingFailureAndSuccess_shouldUpdatePollStateAndLastChecked() {
        OffsetDateTime now = OffsetDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
        String url = "https://poll.example/state-" + UUID.randomUUID();
        repository.save(url, now.minusMinutes(1));

        repository.markPollingFailure(url, now, "timeout", 10, 320);

        Integer retryAfterFailure = jdbcTemplate.queryForObject(
                "select retry_count from poll_state ps join links l on l.id = ps.link_id where l.url = ?",
                Integer.class,
                url);
        assertEquals(1, retryAfterFailure);

        OffsetDateTime nextPollAfterFailure = jdbcTemplate.queryForObject(
                "select next_poll_at from poll_state ps join links l on l.id = ps.link_id where l.url = ?",
                OffsetDateTime.class,
                url);
        assertTrue(nextPollAfterFailure.isAfter(now));

        OffsetDateTime successCheckedAt = now.plusMinutes(1);
        repository.markPollingSuccess(url, successCheckedAt, successCheckedAt.plusMinutes(5));

        Integer retryAfterSuccess = jdbcTemplate.queryForObject(
                "select retry_count from poll_state ps join links l on l.id = ps.link_id where l.url = ?",
                Integer.class,
                url);
        assertEquals(0, retryAfterSuccess);

        OffsetDateTime lastCheckedAt = jdbcTemplate.queryForObject(
                "select last_checked_at from links where url = ?", OffsetDateTime.class, url);
        assertEquals(successCheckedAt.toInstant(), lastCheckedAt.toInstant());
    }

    @Test
    public void updateEtag_thenFindEtagByLink_returnsStoredEtag() {
        OffsetDateTime createdAt = OffsetDateTime.now(clock).minusMinutes(1);
        String url = "https://etag.example/" + UUID.randomUUID();
        repository.save(url, createdAt);

        repository.updateEtag(url, "\"test-etag\"");
        Optional<String> storedEtag = repository.findEtagByLink(url);

        assertTrue(storedEtag.isPresent());
        assertEquals("\"test-etag\"", storedEtag.orElseThrow());
    }

    @Test
    public void findEtagByLink_whenEtagNotSet_returnsEmpty() {
        OffsetDateTime createdAt = OffsetDateTime.now(clock).minusMinutes(1);
        String url = "https://etag-empty.example/" + UUID.randomUUID();
        repository.save(url, createdAt);

        Optional<String> storedEtag = repository.findEtagByLink(url);

        assertTrue(storedEtag.isEmpty());
    }
}

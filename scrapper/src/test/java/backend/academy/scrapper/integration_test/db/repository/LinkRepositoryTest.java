package backend.academy.scrapper.integration_test.db.repository;

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
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void save_thenFindById_returnsStoredLinkWithEmptyTagsAndFilters() {
        OffsetDateTime createdAt = OffsetDateTime.now(clock).minusMinutes(3);
        String url = "https://example.com/" + UUID.randomUUID();

        Long id = repository.save(url, createdAt);
        Optional<Link> found = repository.findById(id);

        assertNotNull(id);
        assertTrue(found.isPresent());
        assertEquals(id, found.get().id());
        assertEquals(URI.create(url), found.get().url());
        long deltaMicros = Math.abs(ChronoUnit.MICROS.between(
            createdAt.toInstant(),
            found.get().createdAt().toInstant()
        ));
        assertTrue(deltaMicros <= 1);
        assertEquals(Set.of(), found.get().tags());
        assertEquals(Set.of(), found.get().filters());
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

        assertTrue(deleted.isPresent());
        assertEquals(id, deleted.get().id());
        assertEquals(URI.create(url), deleted.get().url());
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
}

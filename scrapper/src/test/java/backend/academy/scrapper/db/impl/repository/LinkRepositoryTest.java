package backend.academy.scrapper.db.impl.repository;

import backend.academy.scrapper.config.ApplicationConfig;
import backend.academy.scrapper.db.TestContainersConfiguration;
import backend.academy.scrapper.db.model.Link;
import backend.academy.scrapper.db.repository.FilterRepository;
import backend.academy.scrapper.db.repository.LinkRepository;
import backend.academy.scrapper.db.repository.TagRepository;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import backend.academy.scrapper.mapper.LinkRowMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@Testcontainers
@ActiveProfiles("test")
@Sql("classpath:test-init.sql")
@Import({
    LinkRepository.class,
    ApplicationConfig.class,
    FilterRepository.class,
    TagRepository.class,
    LinkRowMapper.class
})
public class LinkRepositoryTest extends TestContainersConfiguration {

    @Autowired
    private LinkRepository repository;
    @Autowired
    private Clock clock;

    private final String notExistingLink = "https://example.ru";
    private final String existingLink = "https://github.com/21MokseM12/Log-analyzer-Tbank-project";

    @Test
    public void saveLinkSuccess() {
        OffsetDateTime time = OffsetDateTime.now(clock);
        String link = "https://github.com";

        Long id = repository.save(link, time);
        assertNotNull(id);

        Optional<Link> response = repository.findById(id);
        assertTrue(response.isPresent());

        Link responseLink = response.get();
        assertEquals(responseLink.id(), id);
        assertEquals(responseLink.url(), URI.create(link));
        assertEquals(responseLink.createdAt(), time);
    }

    @Test
    public void findIdByLink_thatExists_shouldReturnLink() {
        Optional<Long> linkId = repository.findIdByLink(existingLink);
        assertTrue(linkId.isPresent());
        assertNotNull(linkId.get());
        assertEquals(1L, linkId.get());
    }

    @Test
    public void findIdByLink_thatDoesNotExist_shouldReturnNegativeValue() {
        Optional<Long> linkId = repository.findIdByLink(notExistingLink);
        assertTrue(linkId.isEmpty());
    }

    @Test
    public void findIdByLink_thatIsNull_shouldReturnEmptyOptional() {
        Optional<Long> linkId = repository.findIdByLink(null);
        assertTrue(linkId.isEmpty());
    }

    @Test
    public void whenLinkExists_shouldReturnTrue() {
        boolean existsLink = repository.existsLink(existingLink);
        assertTrue(existsLink);
    }

    @Test
    public void whenLinkDoesNotExist_shouldReturnFalse() {
        boolean existsLink = repository.existsLink(notExistingLink);
        assertFalse(existsLink);
    }

    @Test
    public void whenLinkIsNull_thenReturnFalse() {
        boolean existsLink = repository.existsLink(null);
        assertFalse(existsLink);
    }

    @Test
    public void findById_whenIdExists_shouldReturnLink() {
        Long id = 1L;
        Link expected = new Link(
            1L,
            URI.create("https://github.com/21MokseM12/Log-analyzer-Tbank-project"),
            Set.of("logger", "tinkoff"),
            Set.of("pet-project", "project"),
            OffsetDateTime.of(LocalDate.of(2025, 3, 19), LocalTime.of(10, 30, 0), ZoneOffset.UTC));
        Optional<Link> byId = repository.findById(id);
        assertTrue(byId.isPresent());
        assertNotNull(byId.get());
        assertEquals(expected, byId.get());
    }

    @Test
    public void findById_whenIdDoesNotExist_shouldReturnEmptyOptional() {
        Long id = 100L;
        Optional<Link> byId = repository.findById(id);
        assertTrue(byId.isEmpty());
    }

    @Test
    public void findById_whenIdIsNull_shouldReturnEmptyOptional() {
        Optional<Link> byId = repository.findById(null);
        assertTrue(byId.isEmpty());
    }

    @Test
    public void delete_whenLinkExists_shouldDeleteLink() {
        String link = "https://example.com";
        OffsetDateTime time = OffsetDateTime.now(clock);
        Long id = repository.save(link, time);

        Optional<Link> deleted = repository.delete(id);
        assertTrue(deleted.isPresent());
        assertFalse(repository.existsLink(link));
    }

    @Test
    public void delete_whenLinkDoesNotExist_shouldReturnEmptyOptional() {
        Long id = 12345L;
        Optional<Link> deleted = repository.delete(id);
        assertTrue(deleted.isEmpty());
    }

    @Test
    public void delete_whenLinkIsNull_shouldReturnEmptyOptional() {
        Optional<Link> deleted = repository.delete(null);
        assertTrue(deleted.isEmpty());
    }

    @Test
    public void findAllLinksByUpdateAt_whenAllLinksNeededUpdate_shouldReturnSetOfUris() {
        OffsetDateTime time = OffsetDateTime.of(LocalDate.of(2025, 3, 19), LocalTime.of(10, 35, 0), ZoneOffset.UTC);

        Set<URI> allLinksByUpdatedAt =
            repository.findAllLinksByUpdatedAt(time.minus(Duration.of(1, ChronoUnit.MINUTES)), 0, 5);

        assertNotNull(allLinksByUpdatedAt);
        assertFalse(allLinksByUpdatedAt.isEmpty());
        assertEquals(3, allLinksByUpdatedAt.size());
    }

    @Test
    public void findAllLinksByUpdateAt_whenLinksNeededUpdateExists_shouldReturnSetOfUris() {
        OffsetDateTime time = OffsetDateTime.of(LocalDate.of(2025, 3, 19), LocalTime.of(10, 32, 0), ZoneOffset.UTC);

        Set<URI> allLinksByUpdatedAt =
            repository.findAllLinksByUpdatedAt(time.minus(Duration.of(1, ChronoUnit.MINUTES)), 0, 5);

        assertNotNull(allLinksByUpdatedAt);
        assertFalse(allLinksByUpdatedAt.isEmpty());
        assertEquals(2, allLinksByUpdatedAt.size());
    }

    @Test
    public void findAllLinksByUpdateAt_whenLinkNeededUpdateDoesNotExist_shouldReturnEmptySet() {
        OffsetDateTime time = OffsetDateTime.of(LocalDate.of(2025, 3, 19), LocalTime.of(10, 30, 0), ZoneOffset.UTC);

        Set<URI> allLinksByUpdatedAt =
            repository.findAllLinksByUpdatedAt(time.minus(Duration.of(1, ChronoUnit.MINUTES)), 0, 5);

        assertNotNull(allLinksByUpdatedAt);
        assertTrue(allLinksByUpdatedAt.isEmpty());
    }
}

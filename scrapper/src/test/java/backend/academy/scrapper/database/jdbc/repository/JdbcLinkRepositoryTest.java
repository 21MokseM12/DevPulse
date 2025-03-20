package backend.academy.scrapper.database.jdbc.repository;

import backend.academy.scrapper.config.ApplicationConfig;
import backend.academy.scrapper.database.TestContainersConfiguration;
import backend.academy.scrapper.database.model.Link;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@Testcontainers
@ActiveProfiles("test")
@Import({JdbcLinkRepository.class, ApplicationConfig.class})
@Sql("classpath:test-init.sql")
public class JdbcLinkRepositoryTest extends TestContainersConfiguration {

    @Autowired
    private JdbcLinkRepository repository;

    private final String notExistingLink = "https://example.ru";

    private final String existingLink = "https://github.com/21MokseM12/Log-analyzer-Tbank-project";

    @Test
    public void saveLinkSuccess() {
        AddLinkRequest request = new AddLinkRequest(
            URI.create("https://github.com"),
            Set.of("tag1"),
            Set.of("filter1")
        );
        Link saved = repository.save(request);
        assertNotNull(saved);
        assertEquals(request.link(), saved.url());
        assertEquals(request.tags(), saved.tags());
        assertEquals(request.filters(), saved.filters());
        assertNotNull(saved.id());
    }

    @Test
    public void findByLink_thatExists_shouldReturnLink() {
        Optional<Long> linkId = repository.findByLink(existingLink);
        assertTrue(linkId.isPresent());
        assertNotNull(linkId.get());
        assertEquals(1L, linkId.get());
    }

    @Test
    public void findByLink_thatDoesNotExist_shouldReturnNegativeValue() {
        Optional<Long> linkId = repository.findByLink(notExistingLink);
        assertTrue(linkId.isEmpty());
    }

    @Test
    public void findByLink_thatIsNull_shouldReturnEmptyOptional() {
        Optional<Long> linkId = repository.findByLink(null);
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
            OffsetDateTime.of(LocalDate.of(2025, 3, 19), LocalTime.of(10, 30, 0), ZoneOffset.UTC)
        );
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
        AddLinkRequest request = new AddLinkRequest(
            URI.create("https://example.com"),
            Set.of(),
            Set.of()
        );
        repository.save(request);
        Optional<Link> deleted = repository.delete(request.link().toString());
        assertTrue(deleted.isPresent());
        assertFalse(repository.existsLink(request.link().toString()));
    }

    @Test
    public void delete_whenLinkDoesNotExist_shouldReturnEmptyOptional() {
        Optional<Link> deleted = repository.delete("https://example.com");
        assertTrue(deleted.isEmpty());
    }

    @Test
    public void delete_whenLinkIsNull_shouldReturnEmptyOptional() {
        Optional<Link> deleted = repository.delete(null);
        assertTrue(deleted.isEmpty());
    }

    @Test
    public void findAllLinks_whenIdsIsExists_shouldReturnAllLinks() {
        List<Link> allLinks = repository.findAllLinks(List.of(3L));
        assertNotNull(allLinks);
        assertFalse(allLinks.isEmpty());
    }

    @Test
    public void findAllLinks_whenIdsIsNotExists_shouldReturnEmptyList() {
        List<Link> allLinks = repository.findAllLinks(List.of(1234L));
        assertNotNull(allLinks);
        assertTrue(allLinks.isEmpty());
    }

    @Test
    public void findAllLinks_whenIdsIsEmpty_shouldReturnEmptyList() {
        List<Link> allLinks = repository.findAllLinks(new ArrayList<>());
        assertNotNull(allLinks);
        assertTrue(allLinks.isEmpty());
    }

}

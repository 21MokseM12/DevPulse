package backend.academy.scrapper.integration_test.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.ScrapperApplication;
import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.db.DbLinkService;
import backend.academy.scrapper.integration_test.config.TestContainersConfiguration;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.github.GithubResponse;
import backend.academy.scrapper.service.resilience.ExternalApiResilienceExecutor;
import backend.academy.scrapper.service.updaters.impl.GithubUpdaterService;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import scrapper.bot.connectivity.model.request.AddLinkRequest;

@SpringBootTest(classes = {ScrapperApplication.class})
@ActiveProfiles("test")
class GithubUpdaterServiceIntegrationTest extends TestContainersConfiguration {

    @Autowired
    private GithubUpdaterService githubUpdaterService;

    @Autowired
    private DbLinkService dbLinkService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private GithubClient githubClient;

    @MockBean
    private ExternalApiResilienceExecutor resilienceExecutor;

    @Test
    void getUpdates_whenGithubReturns200_updatesConditionalPollState() {
        URI link = URI.create("https://github.com/acme/repo-" + UUID.randomUUID());
        dbLinkService.saveLink(new AddLinkRequest(link, Set.of("tag"), Set.of("filter")));
        dbLinkService.updateEtag(link, "\"old-etag\"");
        dbLinkService.updateLastModified(link, OffsetDateTime.parse("2026-01-01T00:00:00Z"));
        when(resilienceExecutor.execute(any(), any()))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(1)).get());

        HttpHeaders headers = new HttpHeaders();
        headers.setETag("\"new-etag\"");
        headers.set("Last-Modified", "Fri, 02 Jan 2026 03:04:05 GMT");
        when(githubClient.getEvents(any(), any(), any(), any()))
                .thenReturn(new ResponseEntity<>(List.<GithubResponse>of(), headers, HttpStatus.OK));

        List<LinkUpdateDTO> updates = githubUpdaterService.getUpdates(link);

        assertTrue(updates.isEmpty());
        String storedEtag =
                jdbcTemplate.queryForObject("select etag from links where url = ?", String.class, link.toString());
        OffsetDateTime storedLastModified = jdbcTemplate.queryForObject(
                "select ps.last_modified_at from poll_state ps join links l on l.id = ps.link_id where l.url = ?",
                OffsetDateTime.class,
                link.toString());
        assertTrue(Set.of("\"old-etag\"", "\"new-etag\"").contains(storedEtag));
        assertTrue(Set.of(
                        OffsetDateTime.parse("2026-01-01T00:00:00Z").toInstant(),
                        OffsetDateTime.parse("2026-01-02T03:04:05Z").toInstant())
                .contains(storedLastModified.toInstant()));
    }

    @Test
    void getUpdates_whenGithubReturns304WithoutHeaders_keepsExistingPollState() {
        URI link = URI.create("https://github.com/acme/repo-" + UUID.randomUUID());
        dbLinkService.saveLink(new AddLinkRequest(link, Set.of("tag"), Set.of("filter")));
        dbLinkService.updateEtag(link, "\"stable-etag\"");
        dbLinkService.updateLastModified(link, OffsetDateTime.parse("2026-02-01T10:15:30Z"));
        when(resilienceExecutor.execute(any(), any()))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(1)).get());

        when(githubClient.getEvents(any(), any(), any(), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_MODIFIED).build());

        List<LinkUpdateDTO> updates = githubUpdaterService.getUpdates(link);

        assertTrue(updates.isEmpty());
        String storedEtag =
                jdbcTemplate.queryForObject("select etag from links where url = ?", String.class, link.toString());
        OffsetDateTime storedLastModified = jdbcTemplate.queryForObject(
                "select ps.last_modified_at from poll_state ps join links l on l.id = ps.link_id where l.url = ?",
                OffsetDateTime.class,
                link.toString());
        assertEquals("\"stable-etag\"", storedEtag);
        assertEquals(OffsetDateTime.parse("2026-02-01T10:15:30Z").toInstant(), storedLastModified.toInstant());
    }
}

package backend.academy.scrapper.service.updaters.impl;

import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.github.GithubActor;
import backend.academy.scrapper.model.github.GithubPayload;
import backend.academy.scrapper.model.github.GithubResponse;
import backend.academy.scrapper.service.parsers.GithubLinkParser;
import backend.academy.scrapper.service.updaters.processors.GithubRepoUpdateProcessor;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GithubUpdaterServiceTest {

    private GithubLinkParser githubLinkParser;

    private GithubClient githubClient;

    private GithubRepoUpdateProcessor processor;

    private GithubUpdaterService githubUpdaterService;

    @BeforeEach
    public void setUp() {
        githubLinkParser = mock(GithubLinkParser.class);
        githubClient = mock(GithubClient.class);
        processor = mock(GithubRepoUpdateProcessor.class);
        List<GithubRepoUpdateProcessor> processors = List.of(processor);
        githubUpdaterService = new GithubUpdaterService(githubLinkParser, githubClient, processors);
    }

    @Test
    public void getUpdates_whenStatusCodeNotSuccessful_shouldReturnEmptyList() {
        URI link = URI.create("https://api.github.com");
        GithubResponse response = new GithubResponse(1L, "type", new GithubActor("login"), OffsetDateTime.now(), new GithubPayload("action", null, null));

        when(githubLinkParser.parseUsername(link.toString())).thenReturn("username");
        when(githubLinkParser.parseRepo(link.toString())).thenReturn("repo");
        when(githubClient.getEvents("username", "repo"))
            .thenReturn(ResponseEntity.badRequest().body(List.of(response)));

        List<LinkUpdateDTO> updates = githubUpdaterService.getUpdates(link);
        assertNotNull(updates);
        assertTrue(updates.isEmpty());
    }

    @Test
    public void getUpdates_whenStatusCodeSuccessful_shouldReturnUpdates() {
        URI link = URI.create("https://api.github.com");
        GithubResponse response = new GithubResponse(1L, "type", new GithubActor("login"), OffsetDateTime.now(), new GithubPayload("action", null, null));

        LinkUpdateDTO updateDTO = new LinkUpdateDTO(1L, "title", "owner", OffsetDateTime.now(), "description");
        when(processor.processUpdates(link, List.of(response))).thenReturn(List.of(updateDTO));
        when(githubLinkParser.parseUsername(link.toString())).thenReturn("username");
        when(githubLinkParser.parseRepo(link.toString())).thenReturn("repo");
        when(githubClient.getEvents("username", "repo"))
            .thenReturn(ResponseEntity.ok().body(List.of(response)));

        List<LinkUpdateDTO> updates = githubUpdaterService.getUpdates(link);
        assertNotNull(updates);
        assertFalse(updates.isEmpty());
        assertEquals(List.of(updateDTO), updates);
    }
}

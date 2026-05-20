package backend.academy.scrapper.service.updaters.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.db.DbLinkService;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.github.GithubActor;
import backend.academy.scrapper.model.github.GithubCommit;
import backend.academy.scrapper.model.github.GithubCompareCommit;
import backend.academy.scrapper.model.github.GithubCompareCommitDetails;
import backend.academy.scrapper.model.github.GithubCompareResponse;
import backend.academy.scrapper.model.github.GithubPayload;
import backend.academy.scrapper.model.github.GithubResponse;
import backend.academy.scrapper.service.parsers.GithubLinkParser;
import backend.academy.scrapper.service.resilience.ExternalApiResilienceExecutor;
import backend.academy.scrapper.service.updaters.processors.GithubRepoUpdateProcessor;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class GithubUpdaterServiceTest {

    private GithubLinkParser githubLinkParser;
    private GithubClient githubClient;

    private DbLinkService dbLinkService;
    private ExternalApiResilienceExecutor resilienceExecutor;

    private GithubRepoUpdateProcessor processor;

    private GithubUpdaterService githubUpdaterService;

    @BeforeEach
    public void setUp() {
        githubLinkParser = mock(GithubLinkParser.class);
        githubClient = mock(GithubClient.class);
        dbLinkService = mock(DbLinkService.class);
        resilienceExecutor = mock(ExternalApiResilienceExecutor.class);
        processor = mock(GithubRepoUpdateProcessor.class);
        List<GithubRepoUpdateProcessor> processors = List.of(processor);
        githubUpdaterService =
                new GithubUpdaterService(githubClient, dbLinkService, githubLinkParser, processors, resilienceExecutor);
    }

    @Test
    public void getUpdates_whenStatusCodeNotSuccessful_shouldThrowException() {
        URI link = URI.create("https://api.github.com");
        GithubResponse response = new GithubResponse(
                1L, "type", new GithubActor("login"), OffsetDateTime.now(), new GithubPayload("action", null, null));

        when(dbLinkService.findEtagByLink(link)).thenReturn(Optional.empty());
        when(dbLinkService.findLastModifiedByLink(link)).thenReturn(Optional.empty());
        when(githubLinkParser.parseUsername(link.toString())).thenReturn("username");
        when(githubLinkParser.parseRepo(link.toString())).thenReturn("repo");
        when(resilienceExecutor.execute(eq("github-api"), any()))
                .thenReturn(ResponseEntity.badRequest().body(List.of(response)));

        assertThrows(IllegalStateException.class, () -> githubUpdaterService.getUpdates(link));
    }

    @Test
    public void getUpdates_whenStatusCodeSuccessful_shouldReturnUpdates() {
        URI link = URI.create("https://api.github.com");
        GithubResponse response = new GithubResponse(
                1L, "type", new GithubActor("login"), OffsetDateTime.now(), new GithubPayload("action", null, null));

        LinkUpdateDTO updateDTO = new LinkUpdateDTO(1L, "title", "owner", OffsetDateTime.now(), "description");
        when(processor.processUpdates(eq(link), any())).thenReturn(List.of(updateDTO));
        when(dbLinkService.findEtagByLink(link)).thenReturn(Optional.of("\"old-etag\""));
        when(dbLinkService.findLastModifiedByLink(link))
                .thenReturn(Optional.of(OffsetDateTime.parse("2026-01-01T00:00:00Z")));
        when(githubLinkParser.parseUsername(link.toString())).thenReturn("username");
        when(githubLinkParser.parseRepo(link.toString())).thenReturn("repo");
        HttpHeaders headers = new HttpHeaders();
        headers.setETag("\"new-etag\"");
        headers.set("Last-Modified", "Fri, 02 Jan 2026 03:04:05 GMT");
        when(resilienceExecutor.execute(eq("github-api"), any()))
                .thenReturn(new ResponseEntity<>(List.of(response), headers, HttpStatus.OK));

        List<LinkUpdateDTO> updates = githubUpdaterService.getUpdates(link);
        assertNotNull(updates);
        assertFalse(updates.isEmpty());
        assertEquals(List.of(updateDTO), updates);
        verify(resilienceExecutor).execute(eq("github-api"), any());
        verify(dbLinkService, never()).updateEtag(link, "\"new-etag\"");
        verify(dbLinkService, never()).updateLastModified(link, OffsetDateTime.parse("2026-01-02T03:04:05Z"));
    }

    @Test
    public void getUpdates_whenPushEventHasNoCommits_shouldEnrichEventWithCompareDataBeforeProcessing() {
        URI link = URI.create("https://github.com/acme/repo");
        GithubResponse pushEvent = new GithubResponse(
                10L,
                "PushEvent",
                new GithubActor("octocat"),
                OffsetDateTime.parse("2026-03-06T08:00:00Z"),
                new GithubPayload(
                        "ignored", null, null, "refs/heads/main", "3f5c1e8e2370a49d", "7f9ab17cc840f2b1", List.of()));

        when(dbLinkService.findEtagByLink(link)).thenReturn(Optional.empty());
        when(dbLinkService.findLastModifiedByLink(link)).thenReturn(Optional.empty());
        when(githubLinkParser.parseUsername(link.toString())).thenReturn("acme");
        when(githubLinkParser.parseRepo(link.toString())).thenReturn("repo");
        when(resilienceExecutor.execute(eq("github-api"), any()))
                .thenReturn(ResponseEntity.ok(List.of(pushEvent)))
                .thenReturn(ResponseEntity.ok(new GithubCompareResponse(
                        2,
                        List.of(
                                new GithubCompareCommit(
                                        "3f5c1e8e2370a49d", new GithubCompareCommitDetails("Fix retry logic")),
                                new GithubCompareCommit(
                                        "ab79f99a02c0f901",
                                        new GithubCompareCommitDetails("Add cache invalidation"))))));
        when(processor.processUpdates(eq(link), any())).thenReturn(List.of());

        githubUpdaterService.getUpdates(link);

        ArgumentCaptor<List<GithubResponse>> updatesCaptor = ArgumentCaptor.forClass(List.class);
        verify(processor).processUpdates(eq(link), updatesCaptor.capture());
        List<GithubResponse> processedEvents = updatesCaptor.getValue();
        assertEquals(1, processedEvents.size());
        List<GithubCommit> commits = processedEvents.getFirst().payload().commits();
        assertNotNull(commits);
        assertEquals(2, commits.size());
        assertEquals("Fix retry logic", commits.getFirst().message());
        assertEquals("Add cache invalidation", commits.get(1).message());
        verify(resilienceExecutor, times(2)).execute(eq("github-api"), any());
    }

    @Test
    public void getUpdates_whenCompareRequestFails_shouldProcessOriginalPushEvent() {
        URI link = URI.create("https://github.com/acme/repo");
        GithubResponse pushEvent = new GithubResponse(
                11L,
                "PushEvent",
                new GithubActor("octocat"),
                OffsetDateTime.parse("2026-03-06T08:00:00Z"),
                new GithubPayload(
                        "ignored", null, null, "refs/heads/main", "3f5c1e8e2370a49d", "7f9ab17cc840f2b1", List.of()));

        when(dbLinkService.findEtagByLink(link)).thenReturn(Optional.empty());
        when(dbLinkService.findLastModifiedByLink(link)).thenReturn(Optional.empty());
        when(githubLinkParser.parseUsername(link.toString())).thenReturn("acme");
        when(githubLinkParser.parseRepo(link.toString())).thenReturn("repo");
        when(resilienceExecutor.execute(eq("github-api"), any()))
                .thenReturn(ResponseEntity.ok(List.of(pushEvent)))
                .thenThrow(new IllegalStateException("compare failed"));
        when(processor.processUpdates(eq(link), any())).thenReturn(List.of());

        githubUpdaterService.getUpdates(link);

        ArgumentCaptor<List<GithubResponse>> updatesCaptor = ArgumentCaptor.forClass(List.class);
        verify(processor).processUpdates(eq(link), updatesCaptor.capture());
        List<GithubResponse> processedEvents = updatesCaptor.getValue();
        assertEquals(1, processedEvents.size());
        assertTrue(processedEvents.getFirst().payload().commits().isEmpty());
    }

    @Test
    public void getUpdates_whenResponseIsNotModified_shouldNotProcessPayload() {
        URI link = URI.create("https://api.github.com");

        when(dbLinkService.findEtagByLink(link)).thenReturn(Optional.of("\"same-etag\""));
        when(dbLinkService.findLastModifiedByLink(link))
                .thenReturn(Optional.of(OffsetDateTime.parse("2026-01-02T03:04:05Z")));
        when(githubLinkParser.parseUsername(link.toString())).thenReturn("username");
        when(githubLinkParser.parseRepo(link.toString())).thenReturn("repo");
        HttpHeaders headers = new HttpHeaders();
        headers.setETag("\"same-etag\"");
        headers.set("Last-Modified", "Fri, 02 Jan 2026 03:04:05 GMT");
        when(resilienceExecutor.execute(eq("github-api"), any()))
                .thenReturn(new ResponseEntity<>(null, headers, HttpStatus.NOT_MODIFIED));

        List<LinkUpdateDTO> updates = githubUpdaterService.getUpdates(link);

        assertNotNull(updates);
        assertTrue(updates.isEmpty());
        verifyNoInteractions(processor);
        verify(dbLinkService).updateEtag(link, "\"same-etag\"");
        verify(dbLinkService).updateLastModified(link, OffsetDateTime.parse("2026-01-02T03:04:05Z"));
    }
}

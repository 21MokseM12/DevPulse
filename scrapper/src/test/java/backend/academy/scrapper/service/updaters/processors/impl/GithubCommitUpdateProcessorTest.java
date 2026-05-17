package backend.academy.scrapper.service.updaters.processors.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.enums.GithubActionType;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.UpdateType;
import backend.academy.scrapper.model.github.GithubActor;
import backend.academy.scrapper.model.github.GithubCommit;
import backend.academy.scrapper.model.github.GithubIssue;
import backend.academy.scrapper.model.github.GithubPayload;
import backend.academy.scrapper.model.github.GithubPullRequest;
import backend.academy.scrapper.model.github.GithubResponse;
import backend.academy.scrapper.service.updaters.links.wrappers.impl.GithubLinkService;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GithubCommitUpdateProcessorTest {

    private static final GithubPullRequest PULL_REQUEST = new GithubPullRequest("titlePR", "bodyPR", List.of());

    private static final GithubIssue ISSUE = new GithubIssue("titleIssue", "bodyIssue", List.of());

    private static final GithubActor ACTOR = new GithubActor("actor21");

    @Mock
    private GithubLinkService linkService;

    @InjectMocks
    private GithubCommitUpdateProcessor processor;

    private final URI link = URI.create("link");

    private final OffsetDateTime fixedTime =
            OffsetDateTime.of(LocalDate.of(2025, 3, 26), LocalTime.of(22, 22, 22), ZoneOffset.UTC);

    @Test
    public void processUpdates_whenUpdatesContainsOnlyPushEventsAndEmptyProcessedIdsList_shouldReturnAllUpdates() {
        List<GithubResponse> response = List.of(
                buildPushEvent(1L, "refs/heads/main", "9fceb02", "Fix NPE in scheduler"),
                buildPushEvent(2L, "refs/heads/main", "4477af1", "Add retries for github-api"));
        List<LinkUpdateDTO> expected = List.of(
                new LinkUpdateDTO(
                        1L,
                        "Новый коммит в ветке main",
                        ACTOR.login(),
                        fixedTime,
                        "9fceb02: Fix NPE in scheduler",
                        UpdateType.GITHUB_COMMIT,
                        Set.of()),
                new LinkUpdateDTO(
                        2L,
                        "Новый коммит в ветке main",
                        ACTOR.login(),
                        fixedTime,
                        "4477af1: Add retries for github-api",
                        UpdateType.GITHUB_COMMIT,
                        Set.of()));

        when(linkService.getProcessedCommitIds(link)).thenReturn(List.of());

        List<LinkUpdateDTO> linkUpdateDTOS = processor.processUpdates(link, response);
        assertNotNull(linkUpdateDTOS);
        assertFalse(linkUpdateDTOS.isEmpty());
        assertEquals(expected, linkUpdateDTOS);
    }

    @Test
    public void processUpdates_whenPushEventIsPartOfUpdatesAndProcessedIdsIsEmpty_shouldReturnOnlyPushEvent() {
        List<GithubResponse> response = List.of(
                buildPushEvent(1L, "refs/heads/main", "9fceb02", "Fix NPE in scheduler"),
                new GithubResponse(
                        2L,
                        GithubActionType.ISSUE_EVENT.type(),
                        ACTOR,
                        fixedTime,
                        new GithubPayload("opened", null, ISSUE)),
                new GithubResponse(
                        3L,
                        GithubActionType.PULL_REQUEST_EVENT.type(),
                        ACTOR,
                        fixedTime,
                        new GithubPayload("opened", PULL_REQUEST, null)));
        List<LinkUpdateDTO> expected = List.of(new LinkUpdateDTO(
                1L,
                "Новый коммит в ветке main",
                ACTOR.login(),
                fixedTime,
                "9fceb02: Fix NPE in scheduler",
                UpdateType.GITHUB_COMMIT,
                Set.of()));

        when(linkService.getProcessedCommitIds(link)).thenReturn(List.of());

        List<LinkUpdateDTO> linkUpdateDTOS = processor.processUpdates(link, response);
        assertNotNull(linkUpdateDTOS);
        assertFalse(linkUpdateDTOS.isEmpty());
        assertEquals(expected, linkUpdateDTOS);
    }

    @Test
    public void processUpdates_whenAllPushEventsAlreadyProcessed_shouldReturnEmptyList() {
        List<GithubResponse> response = List.of(
                buildPushEvent(1L, "refs/heads/main", "9fceb02", "Fix NPE in scheduler"),
                buildPushEvent(2L, "refs/heads/main", "4477af1", "Add retries for github-api"));

        when(linkService.getProcessedCommitIds(link)).thenReturn(List.of(1L, 2L));

        List<LinkUpdateDTO> linkUpdateDTOS = processor.processUpdates(link, response);
        assertNotNull(linkUpdateDTOS);
        assertTrue(linkUpdateDTOS.isEmpty());
    }

    @Test
    public void processUpdates_whenPushEventContainsNoCommits_shouldReturnFallbackUpdate() {
        GithubResponse response = new GithubResponse(
                1L,
                GithubActionType.PUSH_EVENT.type(),
                ACTOR,
                fixedTime,
                new GithubPayload("ignored", null, null, "refs/heads/main", "3f5c1e8e2370a49d", null, List.of()));

        when(linkService.getProcessedCommitIds(link)).thenReturn(List.of());

        List<LinkUpdateDTO> linkUpdateDTOS = processor.processUpdates(link, List.of(response));
        assertNotNull(linkUpdateDTOS);
        assertEquals(1, linkUpdateDTOS.size());
        LinkUpdateDTO update = linkUpdateDTOS.getFirst();
        assertEquals(1L, update.id());
        assertEquals("Push в ветке main", update.title());
        assertEquals(
                "Зафиксирован push в репозитории. HEAD: 3f5c1e8 (GitHub Events API не вернул список commits).",
                update.descriptionPreview());
        assertEquals(UpdateType.GITHUB_COMMIT, update.type());
    }

    @AfterEach
    public void checkIfSaveMethodWasInvoked() {
        verify(linkService).saveProcessedIds(eq(link), anyList());
    }

    private GithubResponse buildPushEvent(Long id, String ref, String sha, String message) {
        return new GithubResponse(
                id,
                GithubActionType.PUSH_EVENT.type(),
                ACTOR,
                fixedTime,
                new GithubPayload("ignored", null, null, ref, List.of(new GithubCommit(sha, message))));
    }
}

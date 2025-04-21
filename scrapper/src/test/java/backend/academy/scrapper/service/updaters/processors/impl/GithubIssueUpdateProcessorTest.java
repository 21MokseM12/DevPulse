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
import backend.academy.scrapper.model.github.GithubActor;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GithubIssueUpdateProcessorTest {

    private static final GithubPullRequest PULL_REQUEST = new GithubPullRequest("title", "body");

    private static final GithubIssue ISSUE = new GithubIssue("titleIssue", "bodyIssue");

    private static final GithubActor ACTOR = new GithubActor("actor21");

    @Mock
    private GithubLinkService linkService;

    @InjectMocks
    private GithubIssueUpdateProcessor processor;

    private final URI link = URI.create("link");

    private final OffsetDateTime fixedTime =
            OffsetDateTime.of(LocalDate.of(2025, 3, 26), LocalTime.of(22, 22, 22), ZoneOffset.UTC);

    @Test
    public void processUpdates_whenUpdatesContainsOnlyOpenedIssuesAndEmptyProcessedIdsList_shouldReturnAllUpdates() {
        List<GithubResponse> response = List.of(
                new GithubResponse(
                        1L,
                        GithubActionType.ISSUE_EVENT.type(),
                        ACTOR,
                        fixedTime,
                        new GithubPayload("opened", null, ISSUE)),
                new GithubResponse(
                        2L,
                        GithubActionType.ISSUE_EVENT.type(),
                        ACTOR,
                        fixedTime,
                        new GithubPayload("opened", null, ISSUE)),
                new GithubResponse(
                        3L,
                        GithubActionType.ISSUE_EVENT.type(),
                        ACTOR,
                        fixedTime,
                        new GithubPayload("opened", null, ISSUE)),
                new GithubResponse(
                        4L,
                        GithubActionType.ISSUE_EVENT.type(),
                        ACTOR,
                        fixedTime,
                        new GithubPayload("opened", null, ISSUE)));
        List<LinkUpdateDTO> expected = List.of(
                new LinkUpdateDTO(1L, "titleIssue", ACTOR.login(), fixedTime, "bodyIssue"),
                new LinkUpdateDTO(2L, "titleIssue", ACTOR.login(), fixedTime, "bodyIssue"),
                new LinkUpdateDTO(3L, "titleIssue", ACTOR.login(), fixedTime, "bodyIssue"),
                new LinkUpdateDTO(4L, "titleIssue", ACTOR.login(), fixedTime, "bodyIssue"));

        when(linkService.getProcessedIssueIds(link)).thenReturn(List.of());

        List<LinkUpdateDTO> linkUpdateDTOS = processor.processUpdates(link, response);
        assertNotNull(linkUpdateDTOS);
        assertFalse(linkUpdateDTOS.isEmpty());
        assertEquals(expected, linkUpdateDTOS);
    }

    @Test
    public void processUpdates_whenOpenedIssuesIsPartOfUpdatesAndProcessedIdsIsEmpty_shouldReturnIssuePartOfUpdates() {
        List<GithubResponse> response = List.of(
                new GithubResponse(
                        1L,
                        GithubActionType.PULL_REQUEST_EVENT.type(),
                        ACTOR,
                        fixedTime,
                        new GithubPayload("opened", PULL_REQUEST, null)),
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
                        new GithubPayload("opened", PULL_REQUEST, null)),
                new GithubResponse(
                        4L,
                        GithubActionType.ISSUE_EVENT.type(),
                        ACTOR,
                        fixedTime,
                        new GithubPayload("closed", null, ISSUE)));
        List<LinkUpdateDTO> expected =
                List.of(new LinkUpdateDTO(2L, "titleIssue", ACTOR.login(), fixedTime, "bodyIssue"));

        when(linkService.getProcessedIssueIds(link)).thenReturn(List.of());

        List<LinkUpdateDTO> linkUpdateDTOS = processor.processUpdates(link, response);
        assertNotNull(linkUpdateDTOS);
        assertFalse(linkUpdateDTOS.isEmpty());
        assertEquals(expected, linkUpdateDTOS);
    }

    @Test
    public void processUpdates_whenUpdatesNotContainsOpenedIssuesUpdatesAndProcessedIdsIsEmpty_shouldReturnEmptyList() {
        List<GithubResponse> response = List.of(
                new GithubResponse(
                        1L,
                        GithubActionType.PULL_REQUEST_EVENT.type(),
                        ACTOR,
                        fixedTime,
                        new GithubPayload("opened", PULL_REQUEST, null)),
                new GithubResponse(
                        2L,
                        GithubActionType.ISSUE_EVENT.type(),
                        ACTOR,
                        fixedTime,
                        new GithubPayload("closed", null, ISSUE)),
                new GithubResponse(
                        3L,
                        GithubActionType.ISSUE_EVENT.type(),
                        ACTOR,
                        fixedTime,
                        new GithubPayload("closed", null, ISSUE)),
                new GithubResponse(
                        4L,
                        GithubActionType.PULL_REQUEST_EVENT.type(),
                        ACTOR,
                        fixedTime,
                        new GithubPayload("opened", PULL_REQUEST, null)));

        when(linkService.getProcessedIssueIds(link)).thenReturn(List.of());

        List<LinkUpdateDTO> linkUpdateDTOS = processor.processUpdates(link, response);
        assertNotNull(linkUpdateDTOS);
        assertTrue(linkUpdateDTOS.isEmpty());
    }

    @Test
    public void processUpdates_whenAllUpdatesAlreadyProcessed_shouldReturnEmptyList() {
        List<GithubResponse> response = List.of(
                new GithubResponse(
                        1L,
                        GithubActionType.ISSUE_EVENT.type(),
                        ACTOR,
                        fixedTime,
                        new GithubPayload("opened", null, ISSUE)),
                new GithubResponse(
                        2L,
                        GithubActionType.ISSUE_EVENT.type(),
                        ACTOR,
                        fixedTime,
                        new GithubPayload("opened", null, ISSUE)),
                new GithubResponse(
                        3L,
                        GithubActionType.ISSUE_EVENT.type(),
                        ACTOR,
                        fixedTime,
                        new GithubPayload("opened", null, ISSUE)),
                new GithubResponse(
                        4L,
                        GithubActionType.ISSUE_EVENT.type(),
                        ACTOR,
                        fixedTime,
                        new GithubPayload("opened", null, ISSUE)));

        when(linkService.getProcessedIssueIds(link)).thenReturn(List.of(1L, 2L, 3L, 4L));

        List<LinkUpdateDTO> linkUpdateDTOS = processor.processUpdates(link, response);
        assertNotNull(linkUpdateDTOS);
        assertTrue(linkUpdateDTOS.isEmpty());
    }

    @Test
    public void processUpdates_whenPartOfUpdatesAlreadyProcessed_shouldReturnPartFromIssueUpdates() {
        List<GithubResponse> response = List.of(
                new GithubResponse(
                        1L,
                        GithubActionType.ISSUE_EVENT.type(),
                        ACTOR,
                        fixedTime,
                        new GithubPayload("closed", null, ISSUE)),
                new GithubResponse(
                        2L,
                        GithubActionType.ISSUE_EVENT.type(),
                        ACTOR,
                        fixedTime,
                        new GithubPayload("opened", null, ISSUE)),
                new GithubResponse(
                        3L,
                        GithubActionType.ISSUE_EVENT.type(),
                        ACTOR,
                        fixedTime,
                        new GithubPayload("opened", null, ISSUE)),
                new GithubResponse(
                        4L,
                        GithubActionType.PULL_REQUEST_EVENT.type(),
                        ACTOR,
                        fixedTime,
                        new GithubPayload("opened", PULL_REQUEST, null)));
        List<LinkUpdateDTO> expected =
                List.of(new LinkUpdateDTO(3L, "titleIssue", ACTOR.login(), fixedTime, "bodyIssue"));

        when(linkService.getProcessedIssueIds(link)).thenReturn(List.of(2L));

        List<LinkUpdateDTO> linkUpdateDTOS = processor.processUpdates(link, response);
        assertNotNull(linkUpdateDTOS);
        assertFalse(linkUpdateDTOS.isEmpty());
        assertEquals(expected, linkUpdateDTOS);
    }

    @AfterEach
    public void checkIfSaveMethodWasInvoked() {
        verify(linkService).saveProcessedIds(eq(link), anyList());
    }
}

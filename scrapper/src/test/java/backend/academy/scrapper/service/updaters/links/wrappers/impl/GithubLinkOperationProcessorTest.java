package backend.academy.scrapper.service.updaters.links.wrappers.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.service.LinkOperationProcessor;
import backend.academy.scrapper.enums.ProcessedIdType;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GithubLinkOperationProcessorTest {

    @Mock
    private LinkOperationProcessor linkOperationProcessor;

    @InjectMocks
    private GithubLinkService githubLinkService;

    private final URI link = URI.create("link");

    @Test
    public void getProcessedPullRequestIds_whenAllIdsIsPullRequestType_shouldReturnAllIds() {
        List<ProcessedIdDTO> processedIds = List.of(
                new ProcessedIdDTO(1L, ProcessedIdType.GITHUB_PULL_REQUEST),
                new ProcessedIdDTO(2L, ProcessedIdType.GITHUB_PULL_REQUEST),
                new ProcessedIdDTO(3L, ProcessedIdType.GITHUB_PULL_REQUEST),
                new ProcessedIdDTO(4L, ProcessedIdType.GITHUB_PULL_REQUEST));

        when(linkOperationProcessor.findAllProcessedIds(link)).thenReturn(processedIds);

        List<Long> processedPullRequestIds = githubLinkService.getProcessedPullRequestIds(link);
        assertNotNull(processedPullRequestIds);
        assertFalse(processedPullRequestIds.isEmpty());
        assertEquals(processedIds.stream().map(ProcessedIdDTO::id).toList(), processedPullRequestIds);
    }

    @Test
    public void getProcessedPullRequestIds_whenPartOfIdsIsPullRequestType_shouldReturnPullRequestTypeIdsPart() {
        List<ProcessedIdDTO> processedIds = List.of(
                new ProcessedIdDTO(1L, ProcessedIdType.GITHUB_PULL_REQUEST),
                new ProcessedIdDTO(2L, ProcessedIdType.GITHUB_ISSUE),
                new ProcessedIdDTO(3L, ProcessedIdType.GITHUB_PULL_REQUEST),
                new ProcessedIdDTO(4L, ProcessedIdType.STACKOVERFLOW_ANSWER));

        when(linkOperationProcessor.findAllProcessedIds(link)).thenReturn(processedIds);

        List<Long> processedPullRequestIds = githubLinkService.getProcessedPullRequestIds(link);
        assertNotNull(processedPullRequestIds);
        assertFalse(processedPullRequestIds.isEmpty());
        assertEquals(List.of(1L, 3L), processedPullRequestIds);
    }

    @Test
    public void getProcessedPullRequestIds_whenNotContainsPullRequestIds_shouldReturnEmptyList() {
        List<ProcessedIdDTO> processedIds = List.of(
                new ProcessedIdDTO(1L, ProcessedIdType.STACKOVERFLOW_COMMENT),
                new ProcessedIdDTO(2L, ProcessedIdType.STACKOVERFLOW_ANSWER),
                new ProcessedIdDTO(3L, ProcessedIdType.GITHUB_ISSUE),
                new ProcessedIdDTO(4L, ProcessedIdType.GITHUB_ISSUE));

        when(linkOperationProcessor.findAllProcessedIds(link)).thenReturn(processedIds);

        List<Long> processedPullRequestIds = githubLinkService.getProcessedPullRequestIds(link);
        assertNotNull(processedPullRequestIds);
        assertTrue(processedPullRequestIds.isEmpty());
    }

    @Test
    public void getProcessedIssueIds_whenAllIdsIsIssueType_shouldReturnAllIds() {
        List<ProcessedIdDTO> processedIds = List.of(
                new ProcessedIdDTO(1L, ProcessedIdType.GITHUB_ISSUE),
                new ProcessedIdDTO(2L, ProcessedIdType.GITHUB_ISSUE),
                new ProcessedIdDTO(3L, ProcessedIdType.GITHUB_ISSUE),
                new ProcessedIdDTO(4L, ProcessedIdType.GITHUB_ISSUE));

        when(linkOperationProcessor.findAllProcessedIds(link)).thenReturn(processedIds);

        List<Long> processedPullRequestIds = githubLinkService.getProcessedIssueIds(link);
        assertNotNull(processedPullRequestIds);
        assertFalse(processedPullRequestIds.isEmpty());
        assertEquals(processedIds.stream().map(ProcessedIdDTO::id).toList(), processedPullRequestIds);
    }

    @Test
    public void getProcessedIssueIds_whenPartOfIdsIsIssueType_shouldReturnIssueTypeIdsPart() {
        List<ProcessedIdDTO> processedIds = List.of(
                new ProcessedIdDTO(1L, ProcessedIdType.GITHUB_PULL_REQUEST),
                new ProcessedIdDTO(2L, ProcessedIdType.GITHUB_ISSUE),
                new ProcessedIdDTO(3L, ProcessedIdType.STACKOVERFLOW_COMMENT),
                new ProcessedIdDTO(4L, ProcessedIdType.STACKOVERFLOW_ANSWER));

        when(linkOperationProcessor.findAllProcessedIds(link)).thenReturn(processedIds);

        List<Long> processedPullRequestIds = githubLinkService.getProcessedIssueIds(link);
        assertNotNull(processedPullRequestIds);
        assertFalse(processedPullRequestIds.isEmpty());
        assertEquals(List.of(2L), processedPullRequestIds);
    }

    @Test
    public void getProcessedIssueIds_whenNotContainsIssueIds_shouldReturnEmptyList() {
        List<ProcessedIdDTO> processedIds = List.of(
                new ProcessedIdDTO(1L, ProcessedIdType.STACKOVERFLOW_COMMENT),
                new ProcessedIdDTO(2L, ProcessedIdType.STACKOVERFLOW_ANSWER),
                new ProcessedIdDTO(3L, ProcessedIdType.GITHUB_PULL_REQUEST),
                new ProcessedIdDTO(4L, ProcessedIdType.GITHUB_PULL_REQUEST));

        when(linkOperationProcessor.findAllProcessedIds(link)).thenReturn(processedIds);

        List<Long> processedPullRequestIds = githubLinkService.getProcessedIssueIds(link);
        assertNotNull(processedPullRequestIds);
        assertTrue(processedPullRequestIds.isEmpty());
    }
}

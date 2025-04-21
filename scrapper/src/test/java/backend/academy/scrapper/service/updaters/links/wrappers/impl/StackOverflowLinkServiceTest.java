package backend.academy.scrapper.service.updaters.links.wrappers.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.database.LinkService;
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
public class StackOverflowLinkServiceTest {

    @Mock
    private LinkService linkService;

    @InjectMocks
    private StackOverflowLinkService stackOverflowLinkService;

    private final URI link = URI.create("https://example.com/link");

    @Test
    public void getProcessedCommentsIds_whenAllIdsIsCommentType_shouldReturnAllIds() {
        List<ProcessedIdDTO> processedIds = List.of(
                new ProcessedIdDTO(1L, ProcessedIdType.STACKOVERFLOW_COMMENT),
                new ProcessedIdDTO(2L, ProcessedIdType.STACKOVERFLOW_COMMENT),
                new ProcessedIdDTO(3L, ProcessedIdType.STACKOVERFLOW_COMMENT),
                new ProcessedIdDTO(4L, ProcessedIdType.STACKOVERFLOW_COMMENT));

        when(linkService.findAllProcessedIds(link)).thenReturn(processedIds);

        List<Long> processedPullRequestIds = stackOverflowLinkService.getProcessedCommentsIds(link);
        assertNotNull(processedPullRequestIds);
        assertFalse(processedPullRequestIds.isEmpty());
        assertEquals(processedIds.stream().map(ProcessedIdDTO::id).toList(), processedPullRequestIds);
    }

    @Test
    public void getProcessedCommentsIds_whenPartOfIdsIsCommentType_shouldReturnCommentTypeIdsPart() {
        List<ProcessedIdDTO> processedIds = List.of(
                new ProcessedIdDTO(1L, ProcessedIdType.STACKOVERFLOW_COMMENT),
                new ProcessedIdDTO(2L, ProcessedIdType.GITHUB_ISSUE),
                new ProcessedIdDTO(3L, ProcessedIdType.GITHUB_PULL_REQUEST),
                new ProcessedIdDTO(4L, ProcessedIdType.STACKOVERFLOW_COMMENT));

        when(linkService.findAllProcessedIds(link)).thenReturn(processedIds);

        List<Long> processedPullRequestIds = stackOverflowLinkService.getProcessedCommentsIds(link);
        assertNotNull(processedPullRequestIds);
        assertFalse(processedPullRequestIds.isEmpty());
        assertEquals(List.of(1L, 4L), processedPullRequestIds);
    }

    @Test
    public void getProcessedCommentsIds_whenNotContainsCommentIds_shouldReturnEmptyList() {
        List<ProcessedIdDTO> processedIds = List.of(
                new ProcessedIdDTO(1L, ProcessedIdType.GITHUB_PULL_REQUEST),
                new ProcessedIdDTO(2L, ProcessedIdType.STACKOVERFLOW_ANSWER),
                new ProcessedIdDTO(3L, ProcessedIdType.GITHUB_ISSUE),
                new ProcessedIdDTO(4L, ProcessedIdType.GITHUB_ISSUE));

        when(linkService.findAllProcessedIds(link)).thenReturn(processedIds);

        List<Long> processedPullRequestIds = stackOverflowLinkService.getProcessedCommentsIds(link);
        assertNotNull(processedPullRequestIds);
        assertTrue(processedPullRequestIds.isEmpty());
    }

    @Test
    public void getProcessedAnswersIds_whenAllIdsIsAnswerType_shouldReturnAllIds() {
        List<ProcessedIdDTO> processedIds = List.of(
                new ProcessedIdDTO(1L, ProcessedIdType.STACKOVERFLOW_ANSWER),
                new ProcessedIdDTO(2L, ProcessedIdType.STACKOVERFLOW_ANSWER),
                new ProcessedIdDTO(3L, ProcessedIdType.STACKOVERFLOW_ANSWER),
                new ProcessedIdDTO(4L, ProcessedIdType.STACKOVERFLOW_ANSWER));

        when(linkService.findAllProcessedIds(link)).thenReturn(processedIds);

        List<Long> processedPullRequestIds = stackOverflowLinkService.getProcessedAnswersIds(link);
        assertNotNull(processedPullRequestIds);
        assertFalse(processedPullRequestIds.isEmpty());
        assertEquals(processedIds.stream().map(ProcessedIdDTO::id).toList(), processedPullRequestIds);
    }

    @Test
    public void getProcessedAnswersIds_whenPartOfIdsIsAnswerType_shouldReturnAnswerTypeIdsPart() {
        List<ProcessedIdDTO> processedIds = List.of(
                new ProcessedIdDTO(1L, ProcessedIdType.GITHUB_PULL_REQUEST),
                new ProcessedIdDTO(2L, ProcessedIdType.GITHUB_ISSUE),
                new ProcessedIdDTO(3L, ProcessedIdType.STACKOVERFLOW_COMMENT),
                new ProcessedIdDTO(4L, ProcessedIdType.STACKOVERFLOW_ANSWER));

        when(linkService.findAllProcessedIds(link)).thenReturn(processedIds);

        List<Long> processedPullRequestIds = stackOverflowLinkService.getProcessedAnswersIds(link);
        assertNotNull(processedPullRequestIds);
        assertFalse(processedPullRequestIds.isEmpty());
        assertEquals(List.of(4L), processedPullRequestIds);
    }

    @Test
    public void getProcessedAnswersIds_whenNotContainsAnswerIds_shouldReturnEmptyList() {
        List<ProcessedIdDTO> processedIds = List.of(
                new ProcessedIdDTO(1L, ProcessedIdType.STACKOVERFLOW_COMMENT),
                new ProcessedIdDTO(2L, ProcessedIdType.GITHUB_ISSUE),
                new ProcessedIdDTO(3L, ProcessedIdType.GITHUB_PULL_REQUEST),
                new ProcessedIdDTO(4L, ProcessedIdType.GITHUB_PULL_REQUEST));

        when(linkService.findAllProcessedIds(link)).thenReturn(processedIds);

        List<Long> processedPullRequestIds = stackOverflowLinkService.getProcessedAnswersIds(link);
        assertNotNull(processedPullRequestIds);
        assertTrue(processedPullRequestIds.isEmpty());
    }
}

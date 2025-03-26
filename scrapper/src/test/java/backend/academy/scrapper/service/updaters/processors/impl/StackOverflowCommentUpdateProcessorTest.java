package backend.academy.scrapper.service.updaters.processors.impl;

import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.stackoverflow.StackOverflowCommentItem;
import backend.academy.scrapper.model.stackoverflow.StackOverflowOwner;
import backend.academy.scrapper.model.stackoverflow.StackOverflowQuestionItem;
import backend.academy.scrapper.model.stackoverflow.StackOverflowResponse;
import backend.academy.scrapper.service.updaters.links.wrappers.impl.StackOverflowLinkService;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StackOverflowCommentUpdateProcessorTest {

    public static final StackOverflowOwner OWNER = new StackOverflowOwner("owner");
    @Mock
    private StackOverflowClient client;

    @Mock
    private StackOverflowLinkService linkService;

    @InjectMocks
    private StackOverflowCommentUpdateProcessor processor;

    private final URI link = URI.create("link");

    private final StackOverflowQuestionItem question = new StackOverflowQuestionItem("questionTitle");

    private final Long questionId = 1L;

    private final String site = "stackoverflow";

    private final String filter = "withbody";

    private final OffsetDateTime fixedTime = OffsetDateTime.of(
        LocalDate.of(2025, 3, 26),
        LocalTime.of(23, 12, 0),
        ZoneOffset.UTC
    );

    @Test
    public void processUpdates_whenStatusCodeIsNotOk_thenReturnEmptyList() {
        StackOverflowResponse<StackOverflowCommentItem> response = new StackOverflowResponse<>(List.of(
            new StackOverflowCommentItem(1L, OWNER, fixedTime, "comment")
        ));

        when(client.getCommentsByQuestionId(questionId, site, filter))
            .thenReturn(ResponseEntity.badRequest().body(response));

        List<LinkUpdateDTO> linkUpdateDTOS = processor.processUpdates(link, questionId, question);
        assertNotNull(linkUpdateDTOS);
        assertTrue(linkUpdateDTOS.isEmpty());
        verify(linkService, never()).saveProcessedIds(eq(link), anyList());
    }

    @Test
    public void processUpdates_whenAllUpdatesAlreadyProcessed_shouldReturnEmptyList() {
        StackOverflowResponse<StackOverflowCommentItem> response = new StackOverflowResponse<>(List.of(
            new StackOverflowCommentItem(1L, OWNER, fixedTime, "comment")
        ));

        when(client.getCommentsByQuestionId(questionId, site, filter))
            .thenReturn(ResponseEntity.ok().body(response));
        when(linkService.getProcessedCommentsIds(link)).thenReturn(List.of(1L));

        List<LinkUpdateDTO> linkUpdateDTOS = processor.processUpdates(link, questionId, question);
        assertNotNull(linkUpdateDTOS);
        assertTrue(linkUpdateDTOS.isEmpty());
        verify(linkService).saveProcessedIds(eq(link), anyList());
    }

    @Test
    public void processUpdates_whenPartOfUpdatesAlreadyProcessed_shouldReturnPartOfUpdates() {
        StackOverflowResponse<StackOverflowCommentItem> response = new StackOverflowResponse<>(List.of(
            new StackOverflowCommentItem(1L, OWNER, fixedTime, "comment"),
            new StackOverflowCommentItem(2L, OWNER, fixedTime, "comment"),
            new StackOverflowCommentItem(3L, OWNER, fixedTime, "comment"),
            new StackOverflowCommentItem(4L, OWNER, fixedTime, "comment"),
            new StackOverflowCommentItem(5L, OWNER, fixedTime, "comment")
        ));

        List<LinkUpdateDTO> expected = List.of(
            new LinkUpdateDTO(2L, question.title(), OWNER.username(), fixedTime, "comment"),
            new LinkUpdateDTO(4L, question.title(), OWNER.username(), fixedTime, "comment")
        );

        when(client.getCommentsByQuestionId(questionId, site, filter))
            .thenReturn(ResponseEntity.ok().body(response));
        when(linkService.getProcessedCommentsIds(link)).thenReturn(List.of(1L, 3L, 5L));

        List<LinkUpdateDTO> linkUpdateDTOS = processor.processUpdates(link, questionId, question);
        assertNotNull(linkUpdateDTOS);
        assertFalse(linkUpdateDTOS.isEmpty());
        assertEquals(expected, linkUpdateDTOS);
        verify(linkService).saveProcessedIds(eq(link), anyList());
    }

    @Test
    public void processUpdates_whenAllUpdatesNotProcessedYet_shouldReturnAllUpdates() {
        StackOverflowResponse<StackOverflowCommentItem> response = new StackOverflowResponse<>(List.of(
            new StackOverflowCommentItem(1L, OWNER, fixedTime, "comment"),
            new StackOverflowCommentItem(2L, OWNER, fixedTime, "comment"),
            new StackOverflowCommentItem(3L, OWNER, fixedTime, "comment"),
            new StackOverflowCommentItem(4L, OWNER, fixedTime, "comment"),
            new StackOverflowCommentItem(5L, OWNER, fixedTime, "comment")
        ));

        List<LinkUpdateDTO> expected = List.of(
            new LinkUpdateDTO(1L, question.title(), OWNER.username(), fixedTime, "comment"),
            new LinkUpdateDTO(2L, question.title(), OWNER.username(), fixedTime, "comment"),
            new LinkUpdateDTO(3L, question.title(), OWNER.username(), fixedTime, "comment"),
            new LinkUpdateDTO(4L, question.title(), OWNER.username(), fixedTime, "comment"),
            new LinkUpdateDTO(5L, question.title(), OWNER.username(), fixedTime, "comment")
        );

        when(client.getCommentsByQuestionId(questionId, site, filter))
            .thenReturn(ResponseEntity.ok().body(response));
        when(linkService.getProcessedCommentsIds(link)).thenReturn(List.of());

        List<LinkUpdateDTO> linkUpdateDTOS = processor.processUpdates(link, questionId, question);
        assertNotNull(linkUpdateDTOS);
        assertFalse(linkUpdateDTOS.isEmpty());
        assertEquals(expected, linkUpdateDTOS);
        verify(linkService).saveProcessedIds(eq(link), anyList());
    }
}

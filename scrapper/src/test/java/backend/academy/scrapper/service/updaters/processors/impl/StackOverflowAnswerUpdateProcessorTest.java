package backend.academy.scrapper.service.updaters.processors.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.UpdateType;
import backend.academy.scrapper.model.stackoverflow.StackOverflowAnswerItem;
import backend.academy.scrapper.model.stackoverflow.StackOverflowOwner;
import backend.academy.scrapper.model.stackoverflow.StackOverflowQuestionItem;
import backend.academy.scrapper.model.stackoverflow.StackOverflowResponse;
import backend.academy.scrapper.service.resilience.ExternalApiResilienceExecutor;
import backend.academy.scrapper.service.updaters.links.wrappers.impl.StackOverflowLinkService;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class StackOverflowAnswerUpdateProcessorTest {

    public static final StackOverflowOwner OWNER = new StackOverflowOwner("owner");

    @Mock
    private StackOverflowClient client;

    @Mock
    private StackOverflowLinkService linkService;

    @Mock
    private ExternalApiResilienceExecutor resilienceExecutor;

    @InjectMocks
    private StackOverflowAnswerUpdateProcessor processor;

    private final URI link = URI.create("link");

    private final StackOverflowQuestionItem question = new StackOverflowQuestionItem("questionTitle", List.of());

    private final Long questionId = 1L;

    private final String site = "stackoverflow";

    private final String filter = "withbody";

    private final OffsetDateTime fixedTime =
            OffsetDateTime.of(LocalDate.of(2025, 3, 26), LocalTime.of(23, 12, 0), ZoneOffset.UTC);

    @Test
    public void processUpdates_whenStatusCodeIsNotOk_thenReturnEmptyList() {
        StackOverflowResponse<StackOverflowAnswerItem> response =
                new StackOverflowResponse<>(List.of(new StackOverflowAnswerItem(1L, OWNER, fixedTime, "answer")));

        when(resilienceExecutor.execute(eq("stackoverflow-api"), any()))
                .thenReturn(ResponseEntity.badRequest().body(response));

        List<LinkUpdateDTO> linkUpdateDTOS = processor.processUpdates(link, questionId, question, null);
        assertNotNull(linkUpdateDTOS);
        assertTrue(linkUpdateDTOS.isEmpty());
        verify(linkService, never()).saveProcessedIds(eq(link), anyList());
    }

    @Test
    public void processUpdates_whenAllUpdatesAlreadyProcessed_shouldReturnEmptyList() {
        StackOverflowResponse<StackOverflowAnswerItem> response =
                new StackOverflowResponse<>(List.of(new StackOverflowAnswerItem(1L, OWNER, fixedTime, "answer")));

        when(resilienceExecutor.execute(eq("stackoverflow-api"), any()))
                .thenReturn(ResponseEntity.ok().body(response));
        when(linkService.getProcessedAnswersIds(link)).thenReturn(List.of(1L));

        List<LinkUpdateDTO> linkUpdateDTOS = processor.processUpdates(link, questionId, question, null);
        assertNotNull(linkUpdateDTOS);
        assertTrue(linkUpdateDTOS.isEmpty());
        verify(linkService, never()).saveProcessedIds(eq(link), anyList());
    }

    @Test
    public void processUpdates_whenPartOfUpdatesAlreadyProcessed_shouldReturnPartOfUpdates() {
        StackOverflowResponse<StackOverflowAnswerItem> response = new StackOverflowResponse<>(List.of(
                new StackOverflowAnswerItem(1L, OWNER, fixedTime, "answer"),
                new StackOverflowAnswerItem(2L, OWNER, fixedTime, "answer"),
                new StackOverflowAnswerItem(3L, OWNER, fixedTime, "answer"),
                new StackOverflowAnswerItem(4L, OWNER, fixedTime, "answer"),
                new StackOverflowAnswerItem(5L, OWNER, fixedTime, "answer")));

        List<LinkUpdateDTO> expected = List.of(
                new LinkUpdateDTO(
                        2L,
                        question.title(),
                        OWNER.username(),
                        fixedTime,
                        "answer",
                        UpdateType.STACKOVERFLOW_ANSWER,
                        Set.of(),
                        URI.create("https://stackoverflow.com/questions/1/#answer-2")),
                new LinkUpdateDTO(
                        4L,
                        question.title(),
                        OWNER.username(),
                        fixedTime,
                        "answer",
                        UpdateType.STACKOVERFLOW_ANSWER,
                        Set.of(),
                        URI.create("https://stackoverflow.com/questions/1/#answer-4")));

        when(resilienceExecutor.execute(eq("stackoverflow-api"), any()))
                .thenReturn(ResponseEntity.ok().body(response));
        when(linkService.getProcessedAnswersIds(link)).thenReturn(List.of(1L, 3L, 5L));

        List<LinkUpdateDTO> linkUpdateDTOS = processor.processUpdates(link, questionId, question, null);
        assertNotNull(linkUpdateDTOS);
        assertFalse(linkUpdateDTOS.isEmpty());
        assertEquals(expected, linkUpdateDTOS);
        verify(linkService, never()).saveProcessedIds(eq(link), anyList());
    }

    @Test
    public void processUpdates_whenAllUpdatesNotProcessedYet_shouldReturnAllUpdates() {
        StackOverflowResponse<StackOverflowAnswerItem> response = new StackOverflowResponse<>(List.of(
                new StackOverflowAnswerItem(1L, OWNER, fixedTime, "answer"),
                new StackOverflowAnswerItem(2L, OWNER, fixedTime, "answer"),
                new StackOverflowAnswerItem(3L, OWNER, fixedTime, "answer"),
                new StackOverflowAnswerItem(4L, OWNER, fixedTime, "answer"),
                new StackOverflowAnswerItem(5L, OWNER, fixedTime, "answer")));

        List<LinkUpdateDTO> expected = List.of(
                new LinkUpdateDTO(
                        1L,
                        question.title(),
                        OWNER.username(),
                        fixedTime,
                        "answer",
                        UpdateType.STACKOVERFLOW_ANSWER,
                        Set.of(),
                        URI.create("https://stackoverflow.com/questions/1/#answer-1")),
                new LinkUpdateDTO(
                        2L,
                        question.title(),
                        OWNER.username(),
                        fixedTime,
                        "answer",
                        UpdateType.STACKOVERFLOW_ANSWER,
                        Set.of(),
                        URI.create("https://stackoverflow.com/questions/1/#answer-2")),
                new LinkUpdateDTO(
                        3L,
                        question.title(),
                        OWNER.username(),
                        fixedTime,
                        "answer",
                        UpdateType.STACKOVERFLOW_ANSWER,
                        Set.of(),
                        URI.create("https://stackoverflow.com/questions/1/#answer-3")),
                new LinkUpdateDTO(
                        4L,
                        question.title(),
                        OWNER.username(),
                        fixedTime,
                        "answer",
                        UpdateType.STACKOVERFLOW_ANSWER,
                        Set.of(),
                        URI.create("https://stackoverflow.com/questions/1/#answer-4")),
                new LinkUpdateDTO(
                        5L,
                        question.title(),
                        OWNER.username(),
                        fixedTime,
                        "answer",
                        UpdateType.STACKOVERFLOW_ANSWER,
                        Set.of(),
                        URI.create("https://stackoverflow.com/questions/1/#answer-5")));

        when(resilienceExecutor.execute(eq("stackoverflow-api"), any()))
                .thenReturn(ResponseEntity.ok().body(response));
        when(linkService.getProcessedAnswersIds(link)).thenReturn(List.of());

        List<LinkUpdateDTO> linkUpdateDTOS = processor.processUpdates(link, questionId, question, null);
        assertNotNull(linkUpdateDTOS);
        assertFalse(linkUpdateDTOS.isEmpty());
        assertEquals(expected, linkUpdateDTOS);
        verify(linkService, never()).saveProcessedIds(eq(link), anyList());
    }
}

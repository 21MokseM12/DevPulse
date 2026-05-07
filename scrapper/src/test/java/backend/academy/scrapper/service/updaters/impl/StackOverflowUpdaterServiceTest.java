package backend.academy.scrapper.service.updaters.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.db.DbLinkService;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.stackoverflow.StackOverflowQuestionItem;
import backend.academy.scrapper.model.stackoverflow.StackOverflowResponse;
import backend.academy.scrapper.service.parsers.StackOverflowLinkParser;
import backend.academy.scrapper.service.resilience.ExternalApiResilienceExecutor;
import backend.academy.scrapper.service.updaters.processors.StackOverflowQuestionUpdateProcessor;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

public class StackOverflowUpdaterServiceTest {

    private StackOverflowClient stackOverflowClient;
    private DbLinkService dbLinkService;
    private ExternalApiResilienceExecutor resilienceExecutor;

    private StackOverflowQuestionUpdateProcessor processor;

    private StackOverflowUpdaterService updaterService;

    private final URI link = URI.create("http://www.example.com");

    @BeforeEach
    public void setUp() {
        stackOverflowClient = mock(StackOverflowClient.class);
        dbLinkService = mock(DbLinkService.class);
        resilienceExecutor = mock(ExternalApiResilienceExecutor.class);
        StackOverflowLinkParser stackOverflowLinkParser = mock(StackOverflowLinkParser.class);
        processor = mock(StackOverflowQuestionUpdateProcessor.class);
        List<StackOverflowQuestionUpdateProcessor> processors = List.of(processor);

        updaterService = new StackOverflowUpdaterService(
                stackOverflowClient, dbLinkService, stackOverflowLinkParser, processors, resilienceExecutor);

        when(stackOverflowLinkParser.parseQuestionId(link.toString())).thenReturn(1L);
    }

    @Test
    public void getUpdates_whenStatusCodeNotSuccessful_shouldReturnEmptyList() {
        when(dbLinkService.findLastEventDateByLink(link)).thenReturn(Optional.empty());
        when(resilienceExecutor.execute(eq("stackoverflow-api"), any()))
                .thenReturn(ResponseEntity.badRequest().body(new StackOverflowResponse<>(List.of())));

        List<LinkUpdateDTO> updates = updaterService.getUpdates(link);
        assertNotNull(updates);
        assertTrue(updates.isEmpty());
    }

    @Test
    public void getUpdates_whenStatusCodeSuccessful_shouldReturnUpdates() {
        StackOverflowQuestionItem questionItem = new StackOverflowQuestionItem("title", List.of());
        OffsetDateTime updateTime = OffsetDateTime.now();
        LinkUpdateDTO expected = new LinkUpdateDTO(1L, "title", "owner", updateTime, "desc");

        when(dbLinkService.findLastEventDateByLink(link)).thenReturn(Optional.empty());
        when(resilienceExecutor.execute(eq("stackoverflow-api"), any()))
                .thenReturn(ResponseEntity.ok().body(new StackOverflowResponse<>(List.of(questionItem))));
        when(processor.processUpdates(link, 1L, questionItem, null)).thenReturn(List.of(expected));

        List<LinkUpdateDTO> updates = updaterService.getUpdates(link);
        assertNotNull(updates);
        assertFalse(updates.isEmpty());
        assertEquals(List.of(expected), updates);
        verify(dbLinkService).updateLastEventDate(link, updateTime);
    }

    @Test
    public void getUpdates_whenNoNewUpdates_shouldNotUpdateLastEventDate() {
        OffsetDateTime lastEventDate = OffsetDateTime.now().minusHours(1);
        long fromDate = lastEventDate.toEpochSecond();
        StackOverflowQuestionItem questionItem = new StackOverflowQuestionItem("title", List.of());

        when(dbLinkService.findLastEventDateByLink(link)).thenReturn(Optional.of(lastEventDate));
        when(resilienceExecutor.execute(eq("stackoverflow-api"), any()))
                .thenReturn(ResponseEntity.ok().body(new StackOverflowResponse<>(List.of(questionItem))));
        when(processor.processUpdates(link, 1L, questionItem, fromDate)).thenReturn(List.of());

        List<LinkUpdateDTO> updates = updaterService.getUpdates(link);

        assertNotNull(updates);
        assertTrue(updates.isEmpty());
        verify(dbLinkService).findLastEventDateByLink(link);
        verify(dbLinkService, never()).updateLastEventDate(link, lastEventDate);
    }
}

package backend.academy.scrapper.service.updaters.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.stackoverflow.StackOverflowQuestionItem;
import backend.academy.scrapper.model.stackoverflow.StackOverflowResponse;
import backend.academy.scrapper.service.parsers.StackOverflowLinkParser;
import backend.academy.scrapper.service.updaters.processors.StackOverflowQuestionUpdateProcessor;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

public class StackOverflowUpdaterServiceTest {

    private StackOverflowClient stackOverflowClient;

    private StackOverflowQuestionUpdateProcessor processor;

    private StackOverflowUpdaterService updaterService;

    private final URI link = URI.create("http://www.example.com");

    @BeforeEach
    public void setUp() {
        stackOverflowClient = mock(StackOverflowClient.class);
        StackOverflowLinkParser stackOverflowLinkParser = mock(StackOverflowLinkParser.class);
        processor = mock(StackOverflowQuestionUpdateProcessor.class);
        List<StackOverflowQuestionUpdateProcessor> processors = List.of(processor);

        updaterService = new StackOverflowUpdaterService(stackOverflowClient, stackOverflowLinkParser, processors);

        when(stackOverflowLinkParser.parseQuestionId(link.toString())).thenReturn(1L);
    }

    @Test
    public void getUpdates_whenStatusCodeNotSuccessful_shouldReturnEmptyList() {
        when(stackOverflowClient.getQuestionById(1L, "stackoverflow"))
                .thenReturn(ResponseEntity.badRequest().body(new StackOverflowResponse<>(List.of())));

        List<LinkUpdateDTO> updates = updaterService.getUpdates(link);
        assertNotNull(updates);
        assertTrue(updates.isEmpty());
    }

    @Test
    public void getUpdates_whenStatusCodeSuccessful_shouldReturnUpdates() {
        StackOverflowQuestionItem questionItem = new StackOverflowQuestionItem("title");
        LinkUpdateDTO expected = new LinkUpdateDTO(1L, "title", "owner", OffsetDateTime.now(), "desc");

        when(stackOverflowClient.getQuestionById(1L, "stackoverflow"))
                .thenReturn(ResponseEntity.ok().body(new StackOverflowResponse<>(List.of(questionItem))));
        when(processor.processUpdates(link, 1L, questionItem)).thenReturn(List.of(expected));

        List<LinkUpdateDTO> updates = updaterService.getUpdates(link);
        assertNotNull(updates);
        assertFalse(updates.isEmpty());
        assertEquals(List.of(expected), updates);
    }
}

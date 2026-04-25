package backend.academy.bot.kafka.listener;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import backend.academy.bot.service.notifications.LinkUpdateProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkUpdateKafkaListenerTest {

    @Mock
    private LinkUpdateProcessingService linkUpdateProcessingService;

    private LinkUpdateKafkaListener listener;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        listener = new LinkUpdateKafkaListener(linkUpdateProcessingService, objectMapper);
    }

    @Test
    void consume_validPayload_processesLinkUpdate() throws Exception {
        String payload =
                """
                {
                  "id": 42,
                  "url": "https://github.com/org/repo/pull/42",
                  "title": "PR update",
                  "updateOwner": "octocat",
                  "description": "desc",
                  "creationDate": "2026-04-26T00:00:00Z",
                  "clientsIds": [1, 2]
                }
                """;

        listener.consume(payload);

        verify(linkUpdateProcessingService).process(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void consume_invalidPayload_throwsAndDoesNotProcess() {
        String payload = "{ this is not json }";

        assertThrows(Exception.class, () -> listener.consume(payload));
        verifyNoInteractions(linkUpdateProcessingService);
    }
}

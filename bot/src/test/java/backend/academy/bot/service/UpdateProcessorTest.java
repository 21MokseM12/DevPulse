package backend.academy.bot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.exceptions.InvalidCommandException;
import backend.academy.bot.model.requests.Request;
import backend.academy.bot.service.commands.CommandController;
import backend.academy.bot.service.requests.mapper.RequestMapperFactory;
import com.pengrad.telegrambot.model.Update;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UpdateProcessorTest {

    @Mock
    private CommandController commandController;

    @Mock
    private RequestMapperFactory requestMapperFactory;

    @InjectMocks
    private UpdateProcessor updateProcessor;

    private final Update update = mock(Update.class);

    @Test
    public void whenRequestMap_isSuccess_thenCommandControllerIsCalled() {
        Request request = mock(Request.class);

        when(requestMapperFactory.map(update)).thenReturn(Optional.of(request));

        updateProcessor.createReply(update);
        verify(commandController).process(request);
    }

    @Test
    public void whenRequestMap_isFailure_thenThrowsInvalidCommandException() {
        Request request = mock(Request.class);

        when(requestMapperFactory.map(update)).thenReturn(Optional.empty());

        InvalidCommandException exception =
                assertThrows(InvalidCommandException.class, () -> updateProcessor.createReply(update));

        verify(commandController, times(0)).process(request);
        assertEquals(InvalidCommandException.class, exception.getClass());
        assertEquals("Cannot map request", exception.getMessage());
    }
}

package backend.academy.bot.service;

import backend.academy.bot.exceptions.InvalidCommandException;
import backend.academy.bot.factory.StatefulCommandManagerFactory;
import backend.academy.bot.factory.StatelessCommandManagerFactory;
import backend.academy.bot.service.managers.stateful.StatefulCommandManager;
import backend.academy.bot.service.managers.stateless.StatelessCommandManager;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UpdateProcessorTest {

    private UpdateProcessor updateProcessor;

    private final StatelessCommandManagerFactory statelessCommandManagerFactory = mock(StatelessCommandManagerFactory.class);

    private final StatefulCommandManagerFactory statefulCommandManagerFactory = mock(StatefulCommandManagerFactory.class);

    private final Message message = mock(Message.class);

    private final Update update = mock(Update.class);

    private final SendMessage message1 = new SendMessage(123L, "Hello World1");

    private final SendMessage message2 = new SendMessage(456L, "Hello World2");

    @BeforeEach
    public void setUp() {
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("Simple message text");

        updateProcessor = new UpdateProcessor(
            statefulCommandManagerFactory,
            statelessCommandManagerFactory
        );
    }

    @Test
    public void testStatefulCommandManagerCreateReply() {
        StatefulCommandManager statefulCommandManager = mock(StatefulCommandManager.class);
        when(statefulCommandManagerFactory.get(update)).thenReturn(Optional.of(statefulCommandManager));
        when(statefulCommandManager.createReply(update)).thenReturn(message1);

        SendMessage reply = updateProcessor.createReply(update);

        assertThat(reply).isNotNull();
        assertEquals(message1, reply);
        verify(statefulCommandManagerFactory, times(1)).get(update);
        verify(statefulCommandManager, times(1)).createReply(update);
    }

    @Test
    public void testStatelessCommandManagerCreateReply() {
        StatelessCommandManager statelessCommandManager = mock(StatelessCommandManager.class);
        when(statelessCommandManagerFactory.get(message)).thenReturn(Optional.of(statelessCommandManager));
        when(statelessCommandManager.createReply(update)).thenReturn(message2);

        SendMessage reply = updateProcessor.createReply(update);

        assertThat(reply).isNotNull();
        assertEquals(message2, reply);
        verify(statelessCommandManagerFactory, times(1)).get(message);
        verify(statelessCommandManager, times(1)).createReply(update);
    }

    @Test
    public void testThrowsInvalidCommandException() {
        when(statefulCommandManagerFactory.get(update)).thenReturn(Optional.empty());
        when(statelessCommandManagerFactory.get(message)).thenReturn(Optional.empty());

        InvalidCommandException exception = assertThrows(InvalidCommandException.class,
            () -> updateProcessor.createReply(update));

        assertEquals("Invalid command: Simple message text", exception.getMessage());
        verify(statelessCommandManagerFactory, times(1)).get(message);
        verify(statefulCommandManagerFactory, times(1)).get(update);
    }
}

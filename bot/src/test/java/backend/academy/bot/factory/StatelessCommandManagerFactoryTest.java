package backend.academy.bot.factory;

import backend.academy.bot.commands.Command;
import backend.academy.bot.service.managers.stateless.StatelessCommandManager;
import com.pengrad.telegrambot.model.Message;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatelessCommandManagerFactoryTest {

    private StatelessCommandManagerFactory factory;

    private StatelessCommandManager manager1;

    private Message message;

    @BeforeEach
    public void setUp() {
        manager1 = mock(StatelessCommandManager.class);
        Command command1 = mock(Command.class);
        when(manager1.getCommand()).thenReturn(command1);
        when(command1.apiCommand()).thenReturn("command1");

        List<StatelessCommandManager> managers = List.of(manager1);
        factory = new StatelessCommandManagerFactory(managers);

        message = mock(Message.class);
    }

    @Test
    public void testWhenCommandExists() {
        when(message.text()).thenReturn("command1");
        Optional<StatelessCommandManager> statelessCommandManager = factory.get(message);
        assertTrue(statelessCommandManager.isPresent());
        assertEquals(manager1, statelessCommandManager.get());
    }

    @Test
    public void testWhenCommandIsNotExists() {
        when(message.text()).thenReturn("unknown_command");
        Optional<StatelessCommandManager> statelessCommandManager = factory.get(message);
        assertFalse(statelessCommandManager.isPresent());
    }
}

package backend.academy.bot.factory;

import backend.academy.bot.commands.Command;
import backend.academy.bot.service.managers.stateful.StatefulCommandManager;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StatefulCommandManagerFactoryTest {

    private StatefulCommandManagerFactory factory;

    private StatefulCommandManager manager1;

    private StatefulCommandManager manager2;

    private Message message;

    private Update update;

    @BeforeEach
    void setUp() {
        // Мокируем команды
        Command command1 = mock(Command.class);
        Command command2 = mock(Command.class);

        // Мокируем менеджеров
        manager1 = mock(StatefulCommandManager.class);
        manager2 = mock(StatefulCommandManager.class);

        // Настройка поведения команд в mock
        when(manager1.getCommand()).thenReturn(command1);
        when(manager2.getCommand()).thenReturn(command2);

        when(command1.apiCommand()).thenReturn("command1");
        when(command2.apiCommand()).thenReturn("command2");

        // Мокируем чат с ID
        when(manager1.hasState(anyLong())).thenReturn(true);  // Состояние у manager1
        when(manager2.hasState(anyLong())).thenReturn(false); // У manager2 состояния нет

        // Создаем фабрику с двумя менеджерами
        List<StatefulCommandManager> managers = Arrays.asList(manager1, manager2);
        factory = new StatefulCommandManagerFactory(managers);

        // Мокируем сообщение
        Chat chat = mock(Chat.class);
        message = mock(Message.class);

        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123L);

        update = mock(Update.class);
        when(update.message()).thenReturn(message);
    }

    @Test
    void testGetReturnsCorrectManagerWhenStateIsPresent() {
        // Проверяем, что будет выбран manager1, так как он имеет состояние для чата 123L
        Optional<StatefulCommandManager> result = factory.get(update);
        assertTrue(result.isPresent());
        assertEquals(manager1, result.get());
    }

    @Test
    void testGetReturnsEmptyWhenNoStateMatches() {
        // Ожидаем, что вернется пустое значение, так как оба менеджера не имеют состояния для chat id 123L
        when(manager1.hasState(123L)).thenReturn(false);
        when(manager2.hasState(123L)).thenReturn(false);
        Optional<StatefulCommandManager> result = factory.get(update);
        assertFalse(result.isPresent());
    }

    @Test
    void testGetReturnsManagerByCommandText() {
        // Мокируем сообщение с текстом, который совпадает с apiCommand
        when(message.text()).thenReturn("command1");
        when(manager1.hasState(123L)).thenReturn(false);
        when(manager2.hasState(123L)).thenReturn(false);

        Optional<StatefulCommandManager> result = factory.get(update);
        assertTrue(result.isPresent());
        assertEquals(manager1, result.get());
    }

    @Test
    void testGetReturnsEmptyWhenNoMatchingCommand() {
        // Мокируем сообщение с текстом, который не совпадает с ни одним apiCommand
        when(message.text()).thenReturn("unknown_command");
        when(manager1.hasState(123L)).thenReturn(false);
        when(manager2.hasState(123L)).thenReturn(false);

        Optional<StatefulCommandManager> result = factory.get(update);
        assertFalse(result.isPresent());
    }

    @Test
    public void testGetReturnsManagerByCallbackQuery() {
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        when(update.message()).thenReturn(null);
        when(update.callbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.data()).thenReturn("123_1");

        Optional<StatefulCommandManager> result = factory.get(update);
        assertTrue(result.isPresent());
    }
}

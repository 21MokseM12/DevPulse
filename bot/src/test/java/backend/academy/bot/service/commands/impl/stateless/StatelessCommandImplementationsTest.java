package backend.academy.bot.service.commands.impl.stateless;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StatelessCommandImplementationsTest {

    @Test
    void startCommand_hasExpectedApiAndDescription() {
        StartCommand startCommand = new StartCommand();

        assertEquals("/start", startCommand.apiCommand());
        assertEquals("Начать пользоваться ботом", startCommand.description());
    }

    @Test
    void helpCommand_hasExpectedApiAndDescription() {
        HelpCommand helpCommand = new HelpCommand();

        assertEquals("/help", helpCommand.apiCommand());
        assertEquals("Вывести все доступные команды", helpCommand.description());
    }

    @Test
    void listCommand_hasExpectedApiAndDescription() {
        ListCommand listCommand = new ListCommand();

        assertEquals("/list", listCommand.apiCommand());
        assertEquals("Показать список отслеживаемых ссылок", listCommand.description());
    }
}

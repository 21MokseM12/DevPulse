package backend.academy.bot.service.commands.impl.stateful;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StatefulCommandImplementationsTest {

    @Test
    void trackCommand_hasExpectedApiAndDescription() {
        TrackCommand trackCommand = new TrackCommand();

        assertEquals("/track", trackCommand.apiCommand());
        assertEquals("Начать отслеживание ссылки", trackCommand.description());
    }

    @Test
    void untrackCommand_hasExpectedApiAndDescription() {
        UntrackCommand untrackCommand = new UntrackCommand();

        assertEquals("/untrack", untrackCommand.apiCommand());
        assertEquals("Прекратить отслеживание ссылки", untrackCommand.description());
    }
}

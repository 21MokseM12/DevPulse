package backend.academy.bot.commands.impl;

import backend.academy.bot.commands.Command;
import org.springframework.stereotype.Component;

@Component
public class UntrackCommand implements Command {
    @Override
    public String apiCommand() {
        return "/untrack";
    }

    @Override
    public String description() {
        return "Прекратить отслеживание ссылки";
    }
}

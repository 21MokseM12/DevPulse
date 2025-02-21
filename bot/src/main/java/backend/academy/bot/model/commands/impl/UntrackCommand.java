package backend.academy.bot.model.commands.impl;

import backend.academy.bot.model.commands.Command;
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

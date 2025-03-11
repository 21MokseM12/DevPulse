package backend.academy.bot.service.commands.impl;

import backend.academy.bot.service.commands.Command;
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

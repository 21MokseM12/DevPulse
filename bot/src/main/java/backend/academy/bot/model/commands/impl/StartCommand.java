package backend.academy.bot.model.commands.impl;

import backend.academy.bot.model.commands.Command;
import org.springframework.stereotype.Component;

@Component
public class StartCommand implements Command {
    @Override
    public String apiCommand() {
        return "/start";
    }

    @Override
    public String description() {
        return "Начать пользоваться ботом";
    }
}

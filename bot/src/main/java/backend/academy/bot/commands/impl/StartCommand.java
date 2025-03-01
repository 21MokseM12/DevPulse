package backend.academy.bot.commands.impl;

import backend.academy.bot.commands.Command;
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

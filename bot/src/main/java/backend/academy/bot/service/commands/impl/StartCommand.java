package backend.academy.bot.service.commands.impl;

import backend.academy.bot.service.commands.Command;
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

package backend.academy.bot.commands.impl;

import backend.academy.bot.commands.Command;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand implements Command {
    @Override
    public String apiCommand() {
        return "/help";
    }

    @Override
    public String description() {
        return "Вывести все доступные команды";
    }
}

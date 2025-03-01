package backend.academy.bot.commands.impl;

import backend.academy.bot.commands.Command;
import org.springframework.stereotype.Component;

@Component
public class ListCommand implements Command {
    @Override
    public String apiCommand() {
        return "/list";
    }

    @Override
    public String description() {
        return "Показать список отслеживаемых ссылок";
    }
}

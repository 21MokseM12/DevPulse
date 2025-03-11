package backend.academy.bot.service.commands.impl;

import backend.academy.bot.service.commands.Command;
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

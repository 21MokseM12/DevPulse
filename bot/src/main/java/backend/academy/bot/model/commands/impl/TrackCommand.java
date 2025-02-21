package backend.academy.bot.model.commands.impl;

import backend.academy.bot.model.commands.Command;
import org.springframework.stereotype.Component;

@Component
public class TrackCommand implements Command {
    @Override
    public String apiCommand() {
        return "/track";
    }

    @Override
    public String description() {
        return "Начать отслеживание ссылки";
    }
}

package backend.academy.bot.service.commands.impl.stateful;

import org.springframework.stereotype.Component;

@Component
public class TrackCommand implements StatefulCommand {
    @Override
    public String apiCommand() {
        return "/track";
    }

    @Override
    public String description() {
        return "Начать отслеживание ссылки";
    }
}

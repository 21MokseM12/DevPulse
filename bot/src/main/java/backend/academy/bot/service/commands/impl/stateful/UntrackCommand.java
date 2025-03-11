package backend.academy.bot.service.commands.impl.stateful;

import org.springframework.stereotype.Component;

@Component
public class UntrackCommand implements StatefulCommand {
    @Override
    public String apiCommand() {
        return "/untrack";
    }

    @Override
    public String description() {
        return "Прекратить отслеживание ссылки";
    }
}

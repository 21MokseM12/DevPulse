package backend.academy.bot.service.commands.impl.stateless;

import org.springframework.stereotype.Component;

@Component
public class StartCommand implements StatelessCommand {
    @Override
    public String apiCommand() {
        return "/start";
    }

    @Override
    public String description() {
        return "Начать пользоваться ботом";
    }
}

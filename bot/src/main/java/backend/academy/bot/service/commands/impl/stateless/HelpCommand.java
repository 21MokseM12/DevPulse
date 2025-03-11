package backend.academy.bot.service.commands.impl.stateless;

import org.springframework.stereotype.Component;

@Component
public class HelpCommand implements StatelessCommand {
    @Override
    public String apiCommand() {
        return "/help";
    }

    @Override
    public String description() {
        return "Вывести все доступные команды";
    }
}

package backend.academy.bot.service.commands.impl.stateless;

import org.springframework.stereotype.Component;

@Component
public class ListCommand implements StatelessCommand {
    @Override
    public String apiCommand() {
        return "/list";
    }

    @Override
    public String description() {
        return "Показать список отслеживаемых ссылок";
    }
}

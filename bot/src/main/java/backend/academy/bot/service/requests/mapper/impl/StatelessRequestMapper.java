package backend.academy.bot.service.requests.mapper.impl;

import backend.academy.bot.model.requests.Request;
import backend.academy.bot.model.requests.StatelessRequest;
import backend.academy.bot.service.commands.impl.stateless.StatelessCommand;
import backend.academy.bot.service.requests.mapper.RequestMapper;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatelessRequestMapper implements RequestMapper {

    List<StatelessCommand> commands;

    @Autowired
    public StatelessRequestMapper(List<StatelessCommand> commands) {
        this.commands = commands;
    }

    @Override
    public Request map(Update update) {
        return new StatelessRequest(
                update.message().chat().id(), update.message().text());
    }

    @Override
    public boolean canMap(Update update) {
        if (update.message() == null) {
            return false;
        }
        String text = update.message().text();
        for (StatelessCommand command : commands) {
            if (text.equals(command.apiCommand())) {
                return true;
            }
        }
        return false;
    }
}

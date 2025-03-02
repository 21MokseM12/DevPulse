package backend.academy.bot.service.managers.stateless;

import backend.academy.bot.commands.Command;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class HelpCommandManager implements StatelessCommandManager {

    @Autowired
    @Qualifier("helpCommand")
    private Command helpCommand;

    @Autowired
    private List<Command> commands;

    @Override
    public SendMessage createReply(Update update) {
        StringBuilder reply = new StringBuilder();
        for (Command command : commands) {
            reply.append(command.apiCommand())
                    .append(" - ")
                    .append(command.description())
                    .append("\n");
        }
        return new SendMessage(update.message().chat().id(), reply.toString());
    }

    @Override
    public Command getCommand() {
        return this.helpCommand;
    }
}

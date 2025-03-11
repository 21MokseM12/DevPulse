package backend.academy.bot.service.commands.managers.stateless;

import backend.academy.bot.model.requests.Request;
import backend.academy.bot.service.commands.Command;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class HelpCommandManager implements StatelessCommandManager {

    private final Command helpCommand;

    private final List<Command> commands;

    @Autowired
    public HelpCommandManager(
        @Qualifier("helpCommand") Command helpCommand,
        List<Command> commands
    ) {
        this.helpCommand = helpCommand;
        this.commands = commands;
    }

    @Override
    public SendMessage createReply(Request request) {
        StringBuilder reply = new StringBuilder();
        for (Command command : commands) {
            reply.append(command.apiCommand())
                    .append(" - ")
                    .append(command.description())
                    .append("\n");
        }
        return new SendMessage(request.getChatId(), reply.toString());
    }

    @Override
    public Command getCommand() {
        return this.helpCommand;
    }
}

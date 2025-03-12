package backend.academy.bot.service.commands;

import backend.academy.bot.exceptions.InvalidCommandException;
import backend.academy.bot.model.requests.Request;
import backend.academy.bot.service.commands.managers.CommandManager;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommandController {

    private final List<CommandManager> commandManagers;

    @Autowired
    public CommandController(List<CommandManager> commandManagers) {
        this.commandManagers = commandManagers;
    }

    public SendMessage process(Request request) throws InvalidCommandException {
        return commandManagers.stream()
            .filter(x -> x.canProcess(request))
            .findFirst()
            .orElseThrow(() -> new InvalidCommandException("Invalid command sent by chat with id " + request.getChatId()))
            .createReply(request);
    }
}

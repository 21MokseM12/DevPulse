package backend.academy.bot.service.commands;

import backend.academy.bot.exceptions.InvalidCommandException;
import backend.academy.bot.factory.CommandManagerFactoryRegistry;
import backend.academy.bot.model.requests.Request;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommandController {

    private final CommandManagerFactoryRegistry commandManagerFactoryRegistry;

    @Autowired
    public CommandController(CommandManagerFactoryRegistry commandManagerFactoryRegistry) {
        this.commandManagerFactoryRegistry = commandManagerFactoryRegistry;
    }

    public SendMessage process(Request request) throws InvalidCommandException {
        return commandManagerFactoryRegistry
                .get(request)
                .orElseThrow(
                        () -> new InvalidCommandException("Invalid command in chat with id " + request.getChatId()))
                .createReply(request);
    }
}

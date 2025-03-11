package backend.academy.bot.service;

import backend.academy.bot.exceptions.InvalidCommandException;
import backend.academy.bot.model.requests.Request;
import backend.academy.bot.service.commands.CommandController;
import backend.academy.bot.service.requests.mapper.RequestMapperFactory;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class UpdateProcessor {

    private final CommandController commandController;

    private final RequestMapperFactory requestMapperFactory;

    @Autowired
    public UpdateProcessor(CommandController commandController, RequestMapperFactory requestMapperFactory) {
        this.commandController = commandController;
        this.requestMapperFactory = requestMapperFactory;
    }

    public SendMessage createReply(Update update) throws InvalidCommandException {
        Request request =
                requestMapperFactory.map(update).orElseThrow(() -> new InvalidCommandException("Cannot map request"));
        return commandController.process(request);
    }
}

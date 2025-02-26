package backend.academy.bot.service;

import backend.academy.bot.exceptions.InvalidCommandException;
import backend.academy.bot.factory.StatefulCommandManagerFactory;
import backend.academy.bot.factory.StatelessCommandManagerFactory;
import backend.academy.bot.service.managers.stateful.StatefulCommandManager;
import backend.academy.bot.service.managers.stateless.StatelessCommandManager;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class UpdateProcessor {

    private final StatefulCommandManagerFactory statefulCommandFactory;

    private final StatelessCommandManagerFactory statelessCommandFactory;

    @Autowired
    public UpdateProcessor(
        StatefulCommandManagerFactory statefulCommandFactory,
        StatelessCommandManagerFactory statelessCommandFactory
    ) {
        this.statefulCommandFactory = statefulCommandFactory;
        this.statelessCommandFactory = statelessCommandFactory;
    }

    public SendMessage createReply(Update update) throws InvalidCommandException {
        Optional<StatefulCommandManager> statefulCommandManagerOptional = statefulCommandFactory.get(update.message());
        if (statefulCommandManagerOptional.isPresent()) {
            return statefulCommandManagerOptional.get().createReply(update);
        }
        Optional<StatelessCommandManager> statelessCommandManagerOptional = statelessCommandFactory.get(update.message());
        if (statelessCommandManagerOptional.isPresent()) {
            return statelessCommandManagerOptional.get().createReply(update);
        }
        throw new InvalidCommandException("Invalid command: " + update.message().text());
    }
}

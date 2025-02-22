package backend.academy.bot.service;

import backend.academy.bot.factory.StatefulCommandManagerFactory;
import backend.academy.bot.factory.StatelessCommandManagerFactory;
import backend.academy.bot.model.commands.InvalidCommandException;
import backend.academy.bot.service.managers.stateful.StatefulCommandManager;
import backend.academy.bot.service.managers.stateless.StatelessCommandManager;
import com.pengrad.telegrambot.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Primary
public class MessageProcessor {

    private final StatefulCommandManagerFactory statefulCommandFactory;

    private final StatelessCommandManagerFactory statelessCommandFactory;

    @Autowired
    public MessageProcessor(
        StatefulCommandManagerFactory statefulCommandFactory,
        StatelessCommandManagerFactory statelessCommandFactory
    ) {
        this.statefulCommandFactory = statefulCommandFactory;
        this.statelessCommandFactory = statelessCommandFactory;
    }

    public String createReply(Message message) throws InvalidCommandException {
        Optional<StatefulCommandManager> statefulCommandManagerOptional = statefulCommandFactory.get(message);
        if (statefulCommandManagerOptional.isPresent()) {
            return statefulCommandManagerOptional.get().createReply(message);
        }
        Optional<StatelessCommandManager> statelessCommandManagerOptional = statelessCommandFactory.get(message);
        if (statelessCommandManagerOptional.isPresent()) {
            return statelessCommandManagerOptional.get().createReply(message);
        }
        throw new InvalidCommandException("Invalid command: " + message.text());
    }
}

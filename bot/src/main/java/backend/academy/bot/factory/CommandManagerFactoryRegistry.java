package backend.academy.bot.factory;

import backend.academy.bot.model.requests.Request;
import backend.academy.bot.service.commands.managers.CommandManager;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommandManagerFactoryRegistry {

    List<CommandManagerFactory> factories;

    @Autowired
    public CommandManagerFactoryRegistry(List<CommandManagerFactory> factories) {
        this.factories = factories;
    }

    public Optional<CommandManager> get(Request request) {
        return factories.stream().map(factory -> factory.get(request)).findFirst();
    }
}

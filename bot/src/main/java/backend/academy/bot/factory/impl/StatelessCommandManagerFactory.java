package backend.academy.bot.factory.impl;

import backend.academy.bot.factory.CommandManagerFactory;
import backend.academy.bot.model.requests.Request;
import backend.academy.bot.model.requests.StatelessRequest;
import backend.academy.bot.service.commands.managers.CommandManager;
import backend.academy.bot.service.commands.managers.stateless.StatelessCommandManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatelessCommandManagerFactory implements CommandManagerFactory {

    private final Map<String, StatelessCommandManager> managerMap;

    @Autowired
    public StatelessCommandManagerFactory(List<StatelessCommandManager> managers) {
        this.managerMap = new HashMap<>();
        for (StatelessCommandManager manager : managers) {
            managerMap.put(manager.getCommand().apiCommand(), manager);
        }
    }

    public CommandManager get(Request request) {
        if (request instanceof StatelessRequest statelessRequest) {
            return managerMap.getOrDefault(statelessRequest.command(), null);
        }
        return null;
    }
}

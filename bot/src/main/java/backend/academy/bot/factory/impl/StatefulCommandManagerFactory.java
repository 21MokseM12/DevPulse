package backend.academy.bot.factory.impl;

import backend.academy.bot.factory.CommandManagerFactory;
import backend.academy.bot.model.requests.Request;
import backend.academy.bot.model.requests.StatefulRequest;
import backend.academy.bot.service.commands.managers.CommandManager;
import backend.academy.bot.service.commands.managers.stateful.StatefulCommandManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatefulCommandManagerFactory implements CommandManagerFactory {

    private final Map<String, StatefulCommandManager> managerMap;

    @Autowired
    public StatefulCommandManagerFactory(List<StatefulCommandManager> managers) {
        this.managerMap = new HashMap<>();
        for (StatefulCommandManager manager : managers) {
            managerMap.put(manager.getCommand().apiCommand(), manager);
        }
    }

    public CommandManager get(Request request) {
        if (request instanceof StatefulRequest statefulRequest) {
            for (Map.Entry<String, StatefulCommandManager> entry : managerMap.entrySet()) {
                if (entry.getValue().hasState(request.getChatId())) {
                    return entry.getValue();
                }
            }
            return managerMap.getOrDefault(statefulRequest.getData(), null);
        }
        return null;
    }
}

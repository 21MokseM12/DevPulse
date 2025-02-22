package backend.academy.bot.factory;

import backend.academy.bot.service.managers.stateful.StatefulCommandManager;
import com.pengrad.telegrambot.model.Message;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatefulCommandManagerFactory {

    private final Map<String, StatefulCommandManager> managerMap;

    @Autowired
    public StatefulCommandManagerFactory(List<StatefulCommandManager> managers) {
        this.managerMap = new HashMap<>();
        for (StatefulCommandManager manager : managers) {
            managerMap.put(manager.getCommand().apiCommand(), manager);
        }
    }

    public Optional<StatefulCommandManager> get(Message message) {
        for (Map.Entry<String, StatefulCommandManager> entry : managerMap.entrySet()) {
            if (entry.getValue().hasState(message.chat().id())) {
                return Optional.of(entry.getValue());
            }
        }
        if (managerMap.containsKey(message.text())) {
            return Optional.of(managerMap.get(message.text()));
        } else {
            return Optional.empty();
        }
    }
}

package backend.academy.bot.factory;

import backend.academy.bot.service.managers.stateless.StatelessCommandManager;
import com.pengrad.telegrambot.model.Message;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatelessCommandManagerFactory {

    private final Map<String, StatelessCommandManager> managerMap;

    @Autowired
    public StatelessCommandManagerFactory(List<StatelessCommandManager> managers) {
        this.managerMap = new HashMap<>();
        for (StatelessCommandManager manager : managers) {
            managerMap.put(manager.getCommand().apiCommand(), manager);
        }
    }

    public Optional<StatelessCommandManager> get(Message message) {
        if (managerMap.containsKey(message.text())) {
            return Optional.of(managerMap.get(message.text()));
        } else {
            return Optional.empty();
        }
    }
}

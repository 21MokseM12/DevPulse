package backend.academy.bot.service.commands.impl.stateful.sessions;

import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class UntrackSessionManager {

    private final Set<Long> sessions;

    public UntrackSessionManager() {
        this.sessions = new HashSet<>();
    }

    public Long getSession(Long chatId) {
        return sessions.stream()
            .filter(session -> session.equals(chatId))
            .findFirst()
            .orElse(null);
    }

    public void createSession(Long chatId) {
        sessions.add(chatId);
    }

    public boolean hasSession(Long chatId) {
        return sessions.contains(chatId);
    }

    public void deleteSession(Long chatId) {
        sessions.remove(chatId);
    }
}

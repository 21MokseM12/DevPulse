package backend.academy.bot.service.commands.impl.stateful.sessions;

import backend.academy.bot.enums.TrackCommandStates;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TrackSessionManager {

    private final Map<Long, TrackCommandStates> sessions = new HashMap<>();

    public TrackCommandStates getSession(Long chatId) {
        return sessions.get(chatId);
    }

    public boolean hasSession(Long chatId) {
        return sessions.containsKey(chatId);
    }

    public void createSession(Long chatId) {
        sessions.put(chatId, TrackCommandStates.LINK);
    }

    public void updateSession(Long chatId, TrackCommandStates newState) {
        sessions.put(chatId, newState);
    }

    public void deleteSession(Long chatId) {
        sessions.remove(chatId);
    }
}

package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import jakarta.validation.constraints.NotEmpty;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;

@Repository
public class ClientRepository {

    private static final Map<Long, List<Link>> clients;

    private final ClockProvider clockProvider;

    @Autowired
    public ClientRepository(ClockProvider clockProvider) {
        this.clockProvider = clockProvider;
    }

    static {
        clients = new ConcurrentHashMap<>();
    }

    public boolean existsChat(Long id) {
        return clients.containsKey(id);
    }

    public void saveChat(Long id) {
        clients.put(id, new ArrayList<>());
    }

    public void deleteChat(Long id) {
        clients.remove(id);
    }

    public List<Link> findAllLinks(Long chatId) {
        return clients.get(chatId);
    }

    public Link saveLink(Long chatId, AddLinkRequest link) {
        UUID uuid = UUID.nameUUIDFromBytes(link.link().toString().getBytes());

        Link entity = new Link(
                uuid.getMostSignificantBits(), link.link(), link.tags(), link.filters(), OffsetDateTime.now(clockProvider.getClock()));
        if (!clients.get(chatId).contains(entity)) {
            clients.get(chatId).add(entity);
        }
        return entity;
    }

    public Link deleteLink(Long chatId, RemoveLinkRequest uri) {
        Link unsubscribedLink = clients.get(chatId).stream()
                .filter(link -> link.url().equals(uri.link()))
                .findFirst()
                .orElseThrow();
        clients.get(chatId).remove(unsubscribedLink);
        return unsubscribedLink;
    }

    public Map<Long, List<Link>> findAllLinksByForceCheckDelay(@NotEmpty Duration duration) {
        Map<Long, List<Link>> neededUpdatesClients = new HashMap<>();
        for (Map.Entry<Long, List<Link>> entry : clients.entrySet()) {
            List<Link> clientLinksUpdate = new ArrayList<>();
            entry.getValue().stream()
                    .filter(link -> !OffsetDateTime.now(clockProvider.getClock()).minus(duration).isBefore(link.createdAt()))
                    .forEach(link -> {
                        link.createdAt(OffsetDateTime.now(clockProvider.getClock()));
                        clientLinksUpdate.add(link);
                    });
            neededUpdatesClients.put(entry.getKey(), new ArrayList<>(clientLinksUpdate));
        }
        return neededUpdatesClients;
    }
}

package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import jakarta.validation.constraints.NotEmpty;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;

@Repository
public class ClientRepository {

    private static final Map<Long, List<Link>> clients;

    static {
        clients = new HashMap<>();
    }

    public boolean isClient(Long id) {
        return clients.containsKey(id);
    }

    public void register(Long id) {
        clients.put(id, new ArrayList<>());
    }

    public void unregister(Long id) {
        clients.remove(id);
    }

    public List<Link> findAllLinks(Long chatId) {
        return clients.get(chatId);
    }

    public Link subscribeLink(Long chatId, AddLinkRequest link) {
        Link entity = new Link(
                (long) clients.get(chatId).size() + 1, link.link(), link.tags(), link.filters(), OffsetDateTime.now());
        clients.get(chatId).add(entity);
        return entity;
    }

    public Link unsubscribeLink(Long chatId, RemoveLinkRequest uri) {
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
                    .filter(link -> !OffsetDateTime.now().minus(duration).isBefore(link.createdAt()))
                    .forEach(link -> {
                        link.createdAt(OffsetDateTime.now());
                        clientLinksUpdate.add(link);
                    });
            neededUpdatesClients.put(entry.getKey(), new ArrayList<>(clientLinksUpdate));
        }
        return neededUpdatesClients;
    }
}

package backend.academy.scrapper.repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import scrapper.bot.connectivity.model.Link;
import scrapper.bot.connectivity.model.connectivity.AddLinkRequest;
import scrapper.bot.connectivity.model.connectivity.RemoveLinkRequest;

@Repository
public class ClientRepository {

    private final static Map<Long, List<Link>> clients;

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
            (long) clients.get(chatId).size(),
            link.link(),
            link.tags(),
            link.filters(),
            OffsetDateTime.now()
        );
        clients.get(chatId).add(entity);
        return entity;
    }

    public Link unsubscribeLink(Long chatId, RemoveLinkRequest uri) {
        Link unsubscribedLink = clients.get(chatId).stream()
            .filter(link -> link.url().equals(uri.link()))
            .findFirst().get();
        clients.get(chatId).remove(unsubscribedLink);
        return unsubscribedLink;
    }
}

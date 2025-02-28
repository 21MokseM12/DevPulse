package backend.academy.scrapper.service;

import backend.academy.scrapper.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import scrapper.bot.connectivity.model.Link;
import scrapper.bot.connectivity.model.LinkRequest;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class LinkService {

    private final ClientRepository clientRepository;

    @Autowired
    public LinkService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Optional<List<Link>> findAllByChatId(Long chatId) {
        return clientRepository.findAllLinks(chatId);
    }

    public Optional<Link> subscribe(Long chatId, LinkRequest link) {
        return clientRepository.subscribeLink(chatId, link);
    }

    public Optional<Link> unsubscribe(Long chatId, String uri) {
        return clientRepository.unsubscribeLink(chatId, uri);
    }
}

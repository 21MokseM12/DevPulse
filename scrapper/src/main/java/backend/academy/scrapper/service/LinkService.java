package backend.academy.scrapper.service;

import backend.academy.scrapper.repository.ClientRepository;
import backend.academy.scrapper.utils.LinkLinkResponseConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.model.Link;
import scrapper.bot.connectivity.model.connectivity.AddLinkRequest;
import scrapper.bot.connectivity.model.connectivity.LinkResponse;
import scrapper.bot.connectivity.model.connectivity.RemoveLinkRequest;

@Service
@Slf4j
public class LinkService {

    private final ClientRepository clientRepository;

    @Autowired
    public LinkService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Optional<List<LinkResponse>> findAllByChatId(Long chatId) {
        List<Link> links = clientRepository.findAllLinks(chatId);
        List<LinkResponse> linkResponses = new ArrayList<>();
        for (Link link : links) {
            linkResponses.add(LinkLinkResponseConverter.convert(link));
        }
        return Optional.of(linkResponses);
    }

    public Optional<LinkResponse> subscribe(Long chatId, AddLinkRequest linkRequest) {
        Link link = clientRepository.subscribeLink(chatId, linkRequest);
        log.info("Subscribed to link {}", link);
        return Optional.of(LinkLinkResponseConverter.convert(link));
    }

    public Optional<LinkResponse> unsubscribe(Long chatId, RemoveLinkRequest uri) {
        log.info("Unsubscribed link {}", uri);
        return Optional.of(
            LinkLinkResponseConverter.convert(
                clientRepository.unsubscribeLink(chatId, uri)
            )
        );
    }
}

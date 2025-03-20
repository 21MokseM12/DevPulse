package backend.academy.scrapper.database.jdbc;

import backend.academy.scrapper.database.LinkService;
import backend.academy.scrapper.database.jdbc.mapper.LinkResponseMapper;
import backend.academy.scrapper.database.jdbc.repository.JdbcChatRepository;
import backend.academy.scrapper.database.jdbc.repository.JdbcLinkRepository;
import backend.academy.scrapper.database.jdbc.repository.JdbcLinkToChatRepository;
import backend.academy.scrapper.database.model.Link;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import backend.academy.scrapper.exceptions.LinkNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;

@Service
@Slf4j
public class JdbcLinkService implements LinkService {

    private final JdbcChatRepository chatRepository;

    private final JdbcLinkRepository linkRepository;

    private final JdbcLinkToChatRepository linkToChatRepository;

    @Autowired
    public JdbcLinkService(
            JdbcChatRepository chatRepository,
            JdbcLinkRepository linkRepository,
            JdbcLinkToChatRepository linkToChatRepository) {
        this.chatRepository = chatRepository;
        this.linkRepository = linkRepository;
        this.linkToChatRepository = linkToChatRepository;
    }

    @Override
    @Transactional
    public List<LinkResponse> findAllByChatId(Long chatId) {
        if (!chatRepository.isClient(chatId)) {
            return new ArrayList<>();
        }
        List<Long> linkIds = linkToChatRepository.findAllIdByChatId(chatId);
        List<Link> links = linkRepository.findAllLinks(linkIds);

        return links.stream().map(LinkResponseMapper::map).toList();
    }

    @Override
    @Transactional
    public Optional<LinkResponse> subscribe(Long chatId, AddLinkRequest linkRequest) {
        if (!chatRepository.isClient(chatId)) {
            return Optional.empty();
        }
        Optional<Long> linkId = linkRepository.findByLink(linkRequest.link().toString());
        Link link;
        if (linkId.isEmpty()) {
            link = linkRepository.save(linkRequest);
            linkToChatRepository.subscribeChatOnLink(chatId, link.id());
        } else {
            link = linkRepository.findById(linkId.get()).get();
            if (linkToChatRepository.chatIsSubscribedOnLink(chatId, linkId.get())) {
                return Optional.of(LinkResponseMapper.map(link));
            }
            linkToChatRepository.subscribeChatOnLink(chatId, linkId.get());
        }
        log.info("Subscribed to link {}", link.url().toString());
        return Optional.of(LinkResponseMapper.map(link));
    }

    @Override
    @Transactional
    public Optional<LinkResponse> unsubscribe(Long chatId, RemoveLinkRequest uri) {
        if (!chatRepository.isClient(chatId)) {
            return Optional.empty();
        }
        if (!linkRepository.existsLink(uri.link().toString())) {
            return Optional.empty();
        }
        log.info("User {} unsubscribed link {}", chatId, uri.link());
        Link deletedLink = linkRepository.delete(uri.link().toString())
            .orElseThrow(() -> new LinkNotFoundException("Link " + uri.link() + " was not found"));
        linkToChatRepository.unsubscribe(chatId, deletedLink.id());
        return Optional.of(LinkResponseMapper.map(deletedLink));
    }
}

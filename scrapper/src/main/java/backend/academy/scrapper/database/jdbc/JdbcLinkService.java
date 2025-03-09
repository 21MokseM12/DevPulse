package backend.academy.scrapper.database.jdbc;

import backend.academy.scrapper.database.LinkService;
import backend.academy.scrapper.database.model.Link;
import backend.academy.scrapper.database.repository.jdbc.JdbcChatRepository;
import backend.academy.scrapper.database.repository.jdbc.JdbcLinkRepository;
import backend.academy.scrapper.database.repository.jdbc.JdbcLinkToChatRepository;
import backend.academy.scrapper.utils.LinkLinkResponseConverter;
import java.util.List;
import java.util.Optional;
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
        JdbcLinkToChatRepository linkToChatRepository
    ) {
        this.chatRepository = chatRepository;
        this.linkRepository = linkRepository;
        this.linkToChatRepository = linkToChatRepository;
    }

    @Override
    @Transactional
    public Optional<List<LinkResponse>> findAllByChatId(Long chatId) {
        if (!chatRepository.isClient(chatId)) {
            return Optional.empty();
        }
        List<Long> linkIds = linkToChatRepository.findAllIdByChatId(chatId);
        List<Link> links = linkRepository.findAllLinks(linkIds);

        return Optional.of(links.stream()
            .map(LinkLinkResponseConverter::convert)
            .toList());
    }

    @Override
    @Transactional
    public Optional<LinkResponse> subscribe(Long chatId, AddLinkRequest linkRequest) {
        if (!chatRepository.isClient(chatId)) {
            return Optional.empty();
        }
        Long linkId = linkRepository.findByLink(linkRequest.link().toString());
        Link link;
        if (linkId == -1) {
            link = linkRepository.save(linkRequest);
            linkToChatRepository.subscribeChatOnLink(chatId, link.id());
        } else {
            link = linkRepository.findById(linkId);
            if (linkToChatRepository.chatIsSubscribedOnLink(chatId, linkId)) {
                return Optional.of(LinkLinkResponseConverter.convert(
                    link
                ));
            }
            linkToChatRepository.subscribeChatOnLink(chatId, linkId);
        }
        log.info("Subscribed to link {}", link.url().toString());
        return Optional.of(LinkLinkResponseConverter.convert(link));
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
        Link deletedLink = linkRepository.delete(uri.link().toString());
        linkToChatRepository.unsubscribed(chatId, deletedLink.id());
        return Optional.of(
            LinkLinkResponseConverter.convert(
                deletedLink
            )
        );
    }
}

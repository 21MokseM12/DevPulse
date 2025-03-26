package backend.academy.scrapper.database.jdbc;

import backend.academy.scrapper.config.DatabaseConfig;
import backend.academy.scrapper.database.LinkService;
import backend.academy.scrapper.database.jdbc.mapper.LinkResponseMapper;
import backend.academy.scrapper.database.jdbc.model.Link;
import backend.academy.scrapper.database.jdbc.model.ProcessedId;
import backend.academy.scrapper.database.jdbc.repository.JdbcChatRepository;
import backend.academy.scrapper.database.jdbc.repository.JdbcLinkRepository;
import backend.academy.scrapper.database.jdbc.repository.JdbcLinkToChatRepository;
import backend.academy.scrapper.database.jdbc.repository.JdbcProcessedIdRepository;
import backend.academy.scrapper.enums.ProcessedIdType;
import backend.academy.scrapper.exceptions.LinkNotFoundException;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
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

    private final Clock clock;

    private final DatabaseConfig config;

    private final JdbcChatRepository chatRepository;

    private final JdbcLinkRepository linkRepository;

    private final JdbcLinkToChatRepository linkToChatRepository;

    private final JdbcProcessedIdRepository processedIdRepository;

    @Autowired
    public JdbcLinkService(
        Clock clock, DatabaseConfig config, JdbcChatRepository chatRepository,
        JdbcLinkRepository linkRepository,
        JdbcLinkToChatRepository linkToChatRepository,
        JdbcProcessedIdRepository processedIdRepository) {
        this.clock = clock;
        this.config = config;
        this.chatRepository = chatRepository;
        this.linkRepository = linkRepository;
        this.linkToChatRepository = linkToChatRepository;
        this.processedIdRepository = processedIdRepository;
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
    //todo добавить chatId для вставки на реф в таблицы filters and tags
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

    @Override
    @Transactional(readOnly = true)
    public List<ProcessedIdDTO> findAllProcessedIds(URI link) {
        Optional<Long> optionalLinkId = linkRepository.findByLink(link.toString());
        if (optionalLinkId.isEmpty()) {
            return new ArrayList<>();
        }

        Set<ProcessedId> processedIds = processedIdRepository.findAll(optionalLinkId.get());
        return processedIds.stream().map(id -> new ProcessedIdDTO(
            id.processedId(),
            ProcessedIdType.fromString(id.type())
        )).toList();
    }

    @Override
    @Transactional
    public void saveProcessedIds(URI link, List<ProcessedIdDTO> nowProcessedIds) {
        Optional<Long> optionalLinkId = linkRepository.findByLink(link.toString());
        if (optionalLinkId.isPresent()) {
            Long linkId = optionalLinkId.get();
            processedIdRepository.saveAll(linkId, nowProcessedIds);
        }
    }

    @Override
    public Stream<URI> findAllLinksByForceCheckDelay(Duration duration) {
        int pageNum = 0;
        Stream<URI> resultStream = Stream.empty();
        Set<URI> uris;

        do {
            uris = linkRepository.findAllLinksByUpdatedAt(
                OffsetDateTime.now(clock).minus(duration),
                pageNum,
                config.pageSize()
            );
            resultStream = Stream.concat(resultStream, uris.stream());
        } while (!uris.isEmpty());
        return resultStream;
    }

    @Override
    public List<Long> findSubscribedChats(URI link) {
        Optional<Long> optional = linkRepository.findByLink(link.toString());
        if (optional.isEmpty()) {
            return new ArrayList<>();
        }
        Long linkId = optional.get();
        return linkToChatRepository.findAllByLinkId(linkId);
    }
}

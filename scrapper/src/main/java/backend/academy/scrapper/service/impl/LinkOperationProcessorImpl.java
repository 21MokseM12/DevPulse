package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.config.properties.DatabaseProperty;
import backend.academy.scrapper.db.DbCommonService;
import backend.academy.scrapper.db.DbLinkService;
import backend.academy.scrapper.db.model.Link;
import backend.academy.scrapper.enums.ProcessedIdType;
import backend.academy.scrapper.exceptions.LinkNotFoundException;
import backend.academy.scrapper.mapper.LinkResponseMapper;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import backend.academy.scrapper.service.ChatOperationProcessor;
import backend.academy.scrapper.service.LinkOperationProcessor;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkOperationProcessorImpl implements LinkOperationProcessor {

    private final Clock clock;
    private final DatabaseProperty config;
    private final LinkResponseMapper mapper;
    private final DbLinkService linkService;
    private final DbCommonService commonService;
    private final ChatOperationProcessor chatService;

    @Override
    @Transactional
    public List<LinkResponse> findAllByChatId(Long chatId) {
        if (!chatService.isClient(chatId)) {
            return new ArrayList<>();
        }
        List<Long> linkIds = commonService.findAllLinkIdsByChatId(chatId);
        List<Link> links = linkService.findAllLinks(linkIds);
        return links.stream().map(mapper::toLinkResponse).toList();
    }

    @Override
    @Transactional
    // todo добавить chatId для вставки на реф в таблицы filters and tags
    public Optional<LinkResponse> subscribe(Long chatId, AddLinkRequest linkRequest) {
        try {
            if (!chatService.isClient(chatId)) {
                return Optional.empty();
            }
            Optional<Link> optionalLink = linkService.findByLink(linkRequest.link().toString());
            Link link;
            if (optionalLink.isEmpty()) {
                link = linkService.saveLink(linkRequest);
            } else {
                link = optionalLink.get();
            }
            chatService.subscribeChatOnLink(chatId, link.id(), linkRequest.tags(), linkRequest.filters());
            log.info("Пользователь с id {} подписан на ссылку {}", chatId, link.url());
            return Optional.of(mapper.toLinkResponse(link));
        } catch (Exception e) {
            log.error(
                "Произошла ошибка при подписке на ссылку с клиентом по id {} по запросу: {}",
                chatId,
                linkRequest
            );
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Optional<LinkResponse> unsubscribe(Long chatId, RemoveLinkRequest uri) {
        if (!chatService.isClient(chatId) || !linkService.existsLink(uri.link().toString())) {
            return Optional.empty();
        }
        Link link = linkService.findByLink(uri.link().toString())
            .orElseThrow(() -> new LinkNotFoundException("Ссылка " + uri.link() + " не найдена"));
        chatService.unsubscribe(chatId, link.id());
        log.info("Пользователь с id {} отписался от ссылки {}", chatId, uri.link());
        if (chatService.findAllByLinkId(link.id()).isEmpty()) {
            linkService.delete(uri.link().toString());
        }
        return Optional.of(mapper.toLinkResponse(link));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessedIdDTO> findAllProcessedIds(URI link) {
        return linkService.findByLink(link.toString())
            .map(Link::id)
            .map(commonService::findAllProcessedIdsByLinkId)
            .stream().flatMap(Set::stream)
            .map(id -> new ProcessedIdDTO(id.processedId(), ProcessedIdType.fromString(id.type())))
            .toList();
    }

    @Override
    @Transactional
    public void saveProcessedIds(URI link, List<ProcessedIdDTO> nowProcessedIds) {
        linkService.findByLink(link.toString())
            .map(Link::id)
            .ifPresent(id -> commonService.saveAllProcessedIdsByLinkId(id, nowProcessedIds));
    }

    @Override
    public Set<URI> findAllLinksByForceCheckDelay(Duration duration, int pageNum) {
        return linkService.findAllLinksByUpdatedAt(
            OffsetDateTime.now(clock).minus(duration),
            pageNum,
            config.pageSize()
        );
    }

    @Override
    public List<Long> findSubscribedChats(URI link) {
        return linkService.findByLink(link.toString())
            .map(Link::id)
            .map(chatService::findAllByLinkId)
            .stream().flatMap(List::stream)
            .toList();
    }
}

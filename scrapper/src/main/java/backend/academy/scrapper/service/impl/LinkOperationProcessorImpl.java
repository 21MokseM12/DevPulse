package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.config.properties.DatabaseProperty;
import backend.academy.scrapper.db.DbLinkService;
import backend.academy.scrapper.service.LinkOperationProcessor;
import backend.academy.scrapper.db.model.Link;
import backend.academy.scrapper.db.model.ProcessedId;
import backend.academy.scrapper.db.repository.ChatRepository;
import backend.academy.scrapper.db.repository.LinkToChatRepository;
import backend.academy.scrapper.db.repository.ProcessedIdRepository;
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
    private final ChatRepository chatRepository;
    private final DbLinkService linkService;
    private final LinkToChatRepository linkToChatRepository;
    private final ProcessedIdRepository processedIdRepository;

    @Override
    @Transactional
    public List<LinkResponse> findAllByChatId(Long chatId) {
        if (!chatRepository.isClient(chatId)) {
            return new ArrayList<>();
        }
        List<Long> linkIds = linkToChatRepository.findAllIdByChatId(chatId);
        List<Link> links = linkService.findAllLinks(linkIds);

        return links.stream()
                .map(link -> new LinkResponse(link.id(), link.url(), link.tags(), link.filters()))
                .toList();
    }

    @Override
    @Transactional
    // todo добавить chatId для вставки на реф в таблицы filters and tags
    public Optional<LinkResponse> subscribe(Long chatId, AddLinkRequest linkRequest) {
        if (!chatRepository.isClient(chatId)) {
            return Optional.empty();
        }
        Optional<Long> optionalLinkId =
                linkService.findIdByLink(linkRequest.link().toString());
        Link link;
        if (optionalLinkId.isEmpty()) {
            link = linkService.saveLink(linkRequest);
            linkToChatRepository.subscribeChatOnLink(chatId, link.id());
        } else {
            Long linkId = optionalLinkId.orElseThrow(
                    () -> new LinkNotFoundException("Link " + linkRequest.link() + " was not found"));
            link = linkService
                    .findById(linkId)
                    .orElseThrow(() -> new LinkNotFoundException("Link with id" + linkId + " was not found"));
            if (linkToChatRepository.chatIsSubscribedOnLink(chatId, linkId)) {
                return Optional.of(new LinkResponse(link.id(), link.url(), link.tags(), link.filters()));
            }
            linkToChatRepository.subscribeChatOnLink(chatId, linkId);
        }
        log.info("Subscribed to link {}", link.url().toString());
        return Optional.of(new LinkResponse(link.id(), link.url(), link.tags(), link.filters()));
    }

    @Override
    @Transactional
    public Optional<LinkResponse> unsubscribe(Long chatId, RemoveLinkRequest uri) {
        if (!chatRepository.isClient(chatId)) {
            return Optional.empty();
        }
        if (!linkService.existsLink(uri.link().toString())) {
            return Optional.empty();
        }
        log.info("User {} unsubscribed link {}", chatId, uri.link());
        Link deletedLink = linkService
                .delete(uri.link().toString())
                .orElseThrow(() -> new LinkNotFoundException("Link " + uri.link() + " was not found"));
        linkToChatRepository.unsubscribe(chatId, deletedLink.id());
        return Optional.of(
                new LinkResponse(deletedLink.id(), deletedLink.url(), deletedLink.tags(), deletedLink.filters()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessedIdDTO> findAllProcessedIds(URI link) {
        Optional<Long> optionalLinkId = linkService.findIdByLink(link.toString());
        if (optionalLinkId.isEmpty()) {
            return new ArrayList<>();
        }

        Long linkId = optionalLinkId.orElseThrow(() -> new LinkNotFoundException("Link " + link + " was not found"));
        Set<ProcessedId> processedIds = processedIdRepository.findAll(linkId);
        return processedIds.stream()
                .map(id -> new ProcessedIdDTO(id.processedId(), ProcessedIdType.fromString(id.type())))
                .toList();
    }

    @Override
    @Transactional
    public void saveProcessedIds(URI link, List<ProcessedIdDTO> nowProcessedIds) {
        Optional<Long> optionalLinkId = linkService.findIdByLink(link.toString());
        if (optionalLinkId.isPresent()) {
            Long linkId =
                    optionalLinkId.orElseThrow(() -> new LinkNotFoundException("Link " + link + " was not found"));
            processedIdRepository.saveAll(linkId, nowProcessedIds);
        }
    }

    @Override
    public Set<URI> findAllLinksByForceCheckDelay(Duration duration, int pageNum) {
        return linkService.findAllLinksByUpdatedAt(
                OffsetDateTime.now(clock).minus(duration), pageNum, config.pageSize());
    }

    @Override
    public List<Long> findSubscribedChats(URI link) {
        Optional<Long> optional = linkService.findIdByLink(link.toString());
        if (optional.isEmpty()) {
            return new ArrayList<>();
        }
        Long linkId = optional.orElseThrow();
        return linkToChatRepository.findAllByLinkId(linkId);
    }
}

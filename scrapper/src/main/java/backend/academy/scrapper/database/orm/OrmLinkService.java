package backend.academy.scrapper.database.orm;

import backend.academy.scrapper.config.DatabaseConfig;
import backend.academy.scrapper.database.LinkService;
import backend.academy.scrapper.database.orm.entity.ChatEntity;
import backend.academy.scrapper.database.orm.entity.FilterEntity;
import backend.academy.scrapper.database.orm.entity.LinkEntity;
import backend.academy.scrapper.database.orm.entity.ProcessedIdEntity;
import backend.academy.scrapper.database.orm.entity.TagEntity;
import backend.academy.scrapper.database.orm.mapper.LinkMapper;
import backend.academy.scrapper.database.orm.repository.OrmChatRepository;
import backend.academy.scrapper.database.orm.repository.OrmFilterRepository;
import backend.academy.scrapper.database.orm.repository.OrmLinkRepository;
import backend.academy.scrapper.database.orm.repository.OrmProcessedIdsRepository;
import backend.academy.scrapper.database.orm.repository.OrmTagRepository;
import backend.academy.scrapper.enums.ProcessedIdType;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;

@Service
public class OrmLinkService implements LinkService {

    private final Clock clock;

    private final DatabaseConfig config;

    private final OrmChatRepository ormChatRepository;

    private final OrmLinkRepository ormLinkRepository;

    private final OrmProcessedIdsRepository ormProcessedIdsRepository;

    private final OrmTagRepository ormTagRepository;

    private final OrmFilterRepository ormFilterRepository;

    @Autowired
    public OrmLinkService(
        Clock clock,
        DatabaseConfig config,
        OrmLinkRepository ormLinkRepository,
        OrmChatRepository ormChatRepository,
        OrmProcessedIdsRepository ormProcessedIdsRepository,
        OrmTagRepository ormTagRepository,
        OrmFilterRepository ormFilterRepository
    ) {
        this.clock = clock;
        this.config = config;
        this.ormChatRepository = ormChatRepository;
        this.ormLinkRepository = ormLinkRepository;
        this.ormProcessedIdsRepository = ormProcessedIdsRepository;
        this.ormTagRepository = ormTagRepository;
        this.ormFilterRepository = ormFilterRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<LinkResponse> findAllByChatId(Long chatId) {
        if (!ormChatRepository.existsById(chatId)) {
            return new ArrayList<>();
        }

        return ormChatRepository.findById(chatId)
                .map(entity -> entity.links().stream().toList())
                .orElseGet(ArrayList::new)
                .stream()
                .map(LinkMapper::map)
                .toList();
    }

    @Transactional
    @Override
    //todo добавить chatId для вставки на реф в таблицы filters and tags
    public Optional<LinkResponse> subscribe(Long chatId, AddLinkRequest link) {
        Optional<ChatEntity> chat = ormChatRepository.findById(chatId);
        if (chat.isEmpty()) {
            return Optional.empty();
        }
        LinkEntity linkResponse = ormLinkRepository
                .findByLink(link.link().toString())
                .orElseGet(() -> {
                    LinkEntity linkEntity = new LinkEntity();
                    Set<TagEntity> tagEntities = link.tags().stream()
                        .map(tag -> new TagEntity(null, tag, linkEntity))
                        .map(ormTagRepository::save)
                        .collect(Collectors.toSet());
                    Set<FilterEntity> filterEntities = link.filters().stream()
                        .map(filter -> new FilterEntity(null, filter, linkEntity))
                        .map(ormFilterRepository::save)
                        .collect(Collectors.toSet());

                    return ormLinkRepository.save(LinkMapper.map(
                        linkEntity,
                        link,
                        tagEntities,
                        filterEntities,
                        chat.get()
                    ));
                });
        chat.get().links().add(linkResponse);
        ormChatRepository.save(chat.get());
        return Optional.of(LinkMapper.map(linkResponse));
    }

    @Transactional
    @Override
    public Optional<LinkResponse> unsubscribe(Long chatId, RemoveLinkRequest removeLinkRequest) {
        Optional<ChatEntity> chat = ormChatRepository.findById(chatId);
        if (chat.isEmpty()) {
            return Optional.empty();
        }
        Optional<LinkEntity> linkEntity =
                ormLinkRepository.findByLink(removeLinkRequest.link().toString());
        if (linkEntity.isEmpty()) {
            return Optional.empty();
        }
        if (!chat.get().links().contains(linkEntity.get())) {
            return Optional.empty();
        }
        chat.get().links().remove(linkEntity.get());
        ormChatRepository.save(chat.get());
        return Optional.of(LinkMapper.map(linkEntity.get()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessedIdDTO> findAllProcessedIds(URI link) {
        return ormLinkRepository.findByLink(link.toString())
            .map(linkEntity -> linkEntity.processedIds().stream()
                .map(id -> new ProcessedIdDTO(
                    id.processedId(),
                    ProcessedIdType.fromString(id.type()))
                )
                .toList()
            )
            .orElseGet(ArrayList::new);
    }

    @Override
    @Transactional
    public void saveProcessedIds(URI link, List<ProcessedIdDTO> nowProcessedIds) {
        Optional<LinkEntity> optionalLink = ormLinkRepository.findByLink(link.toString());
        optionalLink.ifPresent(linkEntity -> {
            nowProcessedIds.forEach(id -> {
                ProcessedIdEntity entity = ormProcessedIdsRepository.save(new ProcessedIdEntity(
                    id.id(),
                    id.type().type(),
                    linkEntity
                ));
                linkEntity.processedIds().add(entity);
            });
            ormLinkRepository.save(linkEntity);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Stream<URI> findAllLinksByForceCheckDelay(Duration duration) {
        int pageNum = 0;
        Page<LinkEntity> page;
        Stream<LinkEntity> resultStream = Stream.empty();

        do {
            Pageable pageable = PageRequest.of(pageNum, config.pageSize(), Sort.by("updatedAt").descending());
            page = ormLinkRepository.findLinkEntitiesByUpdatedAtBefore(
                OffsetDateTime.now(clock).minus(duration),
                pageable
            );
            resultStream = Stream.concat(resultStream, page.stream());
            pageNum++;
        } while (page.hasNext());
        return resultStream.map(entity -> URI.create(entity.link()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findSubscribedChats(URI link) {
        return ormLinkRepository.findByLink(link.toString())
            .map(entity -> entity.chats().stream().toList())
            .orElseGet(ArrayList::new)
            .stream()
            .map(ChatEntity::id)
            .toList();
    }
}

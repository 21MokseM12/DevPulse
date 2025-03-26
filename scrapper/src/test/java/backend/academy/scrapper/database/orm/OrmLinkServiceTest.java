package backend.academy.scrapper.database.orm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.config.DatabaseConfig;
import backend.academy.scrapper.database.orm.entity.ChatEntity;
import backend.academy.scrapper.database.orm.entity.FilterEntity;
import backend.academy.scrapper.database.orm.entity.LinkEntity;
import backend.academy.scrapper.database.orm.entity.ProcessedIdEntity;
import backend.academy.scrapper.database.orm.entity.TagEntity;
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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;

@ExtendWith(MockitoExtension.class)
public class OrmLinkServiceTest {

    @Mock
    private DatabaseConfig config;

    @Mock
    private Clock clock;

    @Mock
    private OrmLinkRepository linkRepository;

    @Mock
    private OrmChatRepository ormChatRepository;

    @Mock
    private OrmTagRepository ormTagRepository;

    @Mock
    private OrmFilterRepository ormFilterRepository;

    @Mock
    private OrmProcessedIdsRepository processedIdRepository;

    @InjectMocks
    private OrmLinkService linkService;

    @Test
    public void findAllById_whenIdExist_returnNotEmptyList() {
        Long id = 1L;
        LinkResponse expected = new LinkResponse(1L, URI.create("link"), Set.of("tag"), Set.of("filter"));

        when(ormChatRepository.existsById(id)).thenReturn(true);
        when(ormChatRepository.findById(id))
                .thenReturn(Optional.of(new ChatEntity(
                        1L,
                        Set.of(new LinkEntity(
                                1L,
                                "link",
                                OffsetDateTime.now(),
                                Set.of(new TagEntity("tag")),
                                Set.of(new FilterEntity("filter")),
                                Set.of(new ChatEntity(1L)),
                                Set.of(new ProcessedIdEntity()))))));

        List<LinkResponse> allByChatId = linkService.findAllByChatId(id);
        assertNotNull(allByChatId);
        assertFalse(allByChatId.isEmpty());
        assertEquals(List.of(expected), allByChatId);
    }

    @Test
    public void findAllById_whenIdNotExist_returnEmptyList() {
        Long id = 1L;
        when(ormChatRepository.existsById(id)).thenReturn(false);

        List<LinkResponse> allByChatId = linkService.findAllByChatId(id);
        assertNotNull(allByChatId);
        assertTrue(allByChatId.isEmpty());
    }

    @Test
    public void subscribe_whenChatExist_LinkExist_returnLinkResponse() {
        Long id = 1L;
        AddLinkRequest request = new AddLinkRequest(URI.create("link"), Set.of("tag"), Set.of("filter"));
        LinkResponse expected = new LinkResponse(1L, URI.create("link"), Set.of("tag"), Set.of("filter"));
        Set<LinkEntity> set = new HashSet<>();
        set.add(new LinkEntity(
                1L,
                "link",
                OffsetDateTime.now(),
                Set.of(new TagEntity("tag")),
                Set.of(new FilterEntity("filter")),
                Set.of(new ChatEntity(1L)),
                Set.of(new ProcessedIdEntity())));

        when(ormChatRepository.findById(id)).thenReturn(Optional.of(new ChatEntity(1L, set)));
        when(linkRepository.findByLink(request.link().toString()))
                .thenReturn(Optional.of(new LinkEntity(
                        1L,
                        "link",
                        OffsetDateTime.now(),
                        Set.of(new TagEntity("tag")),
                        Set.of(new FilterEntity("filter")),
                        Set.of(new ChatEntity(1L)),
                        Set.of(new ProcessedIdEntity()))));

        Optional<LinkResponse> response = linkService.subscribe(id, request);
        assertNotNull(response);
        assertTrue(response.isPresent());
        assertEquals(expected, response.get());
    }

    @Test
    public void subscribe_whenChatExist_LinkNotExist_returnLinkResponseAndSaveLink() {
        Long id = 1L;
        AddLinkRequest request = new AddLinkRequest(URI.create("link"), Set.of("tag"), Set.of("filter"));
        LinkResponse expected = new LinkResponse(1L, URI.create("link"), Set.of("tag"), Set.of("filter"));
        Set<LinkEntity> set = new HashSet<>();
        set.add(new LinkEntity(
                1L,
                "link",
                OffsetDateTime.now(),
                Set.of(new TagEntity("tag")),
                Set.of(new FilterEntity("filter")),
                Set.of(new ChatEntity(1L)),
                Set.of(new ProcessedIdEntity())));

        when(ormChatRepository.findById(id)).thenReturn(Optional.of(new ChatEntity(1L, set)));
        when(linkRepository.findByLink(request.link().toString())).thenReturn(Optional.empty());
        when(linkRepository.save(any())).thenReturn(set.iterator().next());
        when(ormTagRepository.save(any())).thenReturn(new TagEntity(1L, "tag", new LinkEntity()));
        when(ormFilterRepository.save(any())).thenReturn(new FilterEntity(1L, "filter", new LinkEntity()));

        Optional<LinkResponse> response = linkService.subscribe(id, request);
        assertNotNull(response);
        assertTrue(response.isPresent());
        assertEquals(expected, response.get());
        verify(linkRepository, times(1)).save(any());
        verify(ormTagRepository, times(1)).save(any());
        verify(ormFilterRepository, times(1)).save(any());
    }

    @Test
    public void subscribe_whenChatNotExist_returnEmptyOptional() {
        Long id = 1L;
        AddLinkRequest request = new AddLinkRequest(URI.create("link"), Set.of("tag"), Set.of("filter"));

        when(ormChatRepository.findById(id)).thenReturn(Optional.empty());

        Optional<LinkResponse> response = linkService.subscribe(id, request);
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    public void unsubscribe_whenChatNotExist_returnEmptyOptional() {
        Long id = 1L;
        RemoveLinkRequest request = new RemoveLinkRequest(URI.create("link"));

        when(ormChatRepository.findById(id)).thenReturn(Optional.empty());

        Optional<LinkResponse> unsubscribed = linkService.unsubscribe(id, request);
        assertNotNull(unsubscribed);
        assertTrue(unsubscribed.isEmpty());
    }

    @Test
    public void unsubscribe_whenLinkNotExist_returnEmptyOptional() {
        Long id = 1L;
        RemoveLinkRequest request = new RemoveLinkRequest(URI.create("link"));
        Set<LinkEntity> set = new HashSet<>();
        set.add(new LinkEntity(
                1L,
                "link",
                OffsetDateTime.now(),
                Set.of(new TagEntity("tag")),
                Set.of(new FilterEntity("filter")),
                Set.of(new ChatEntity(1L)),
                Set.of(new ProcessedIdEntity())));
        ChatEntity chatEntity = new ChatEntity(1L, set);

        when(ormChatRepository.findById(id)).thenReturn(Optional.of(chatEntity));
        when(linkRepository.findByLink(request.link().toString())).thenReturn(Optional.empty());

        Optional<LinkResponse> unsubscribed = linkService.unsubscribe(id, request);
        assertNotNull(unsubscribed);
        assertTrue(unsubscribed.isEmpty());
    }

    @Test
    public void unsubscribe_whenChatNotSubscribedOnLink_returnEmptyOptional() {
        Long id = 1L;
        RemoveLinkRequest request = new RemoveLinkRequest(URI.create("link"));
        Set<LinkEntity> set = new HashSet<>();
        set.add(new LinkEntity(
                1L,
                "link",
                OffsetDateTime.now(),
                Set.of(new TagEntity("tag")),
                Set.of(new FilterEntity("filter")),
                Set.of(new ChatEntity(1L)),
                Set.of(new ProcessedIdEntity())));
        ChatEntity chatEntity = new ChatEntity(1L, set);
        LinkEntity linkEntity = new LinkEntity(
                2L,
                "link",
                OffsetDateTime.now(),
                Set.of(new TagEntity("tag")),
                Set.of(new FilterEntity("filter")),
                Set.of(new ChatEntity(1L)),
                Set.of(new ProcessedIdEntity()));

        when(ormChatRepository.findById(id)).thenReturn(Optional.of(chatEntity));
        when(linkRepository.findByLink(request.link().toString())).thenReturn(Optional.of(linkEntity));

        Optional<LinkResponse> unsubscribed = linkService.unsubscribe(id, request);
        assertNotNull(unsubscribed);
        assertTrue(unsubscribed.isEmpty());
    }

    @Test
    public void unsubscribe_whenChatExist_LinkExist_ChatSubscribedOnLink_returnLinkResponse() {
        Long id = 1L;
        RemoveLinkRequest request = new RemoveLinkRequest(URI.create("link"));
        Set<LinkEntity> set = new HashSet<>();
        set.add(new LinkEntity(
                1L,
                "link",
                OffsetDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC),
                Set.of(new TagEntity("tag")),
                Set.of(new FilterEntity("filter")),
                Set.of(new ChatEntity(1L)),
                Set.of(new ProcessedIdEntity())));
        ChatEntity chatEntity = new ChatEntity(1L, set);
        LinkEntity linkEntity = new LinkEntity(
                1L,
                "link",
                OffsetDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC),
                Set.of(new TagEntity("tag")),
                Set.of(new FilterEntity("filter")),
                Set.of(new ChatEntity(1L)),
                Set.of(new ProcessedIdEntity()));
        LinkResponse expected = new LinkResponse(1L, URI.create("link"), Set.of("tag"), Set.of("filter"));

        when(ormChatRepository.findById(id)).thenReturn(Optional.of(chatEntity));
        when(linkRepository.findByLink(request.link().toString())).thenReturn(Optional.of(linkEntity));

        Optional<LinkResponse> unsubscribed = linkService.unsubscribe(id, request);
        assertNotNull(unsubscribed);
        assertTrue(unsubscribed.isPresent());
        assertEquals(expected, unsubscribed.get());
    }

    @Test
    public void findAllProcessedIds_whenLinkIsNoExists_shouldReturnEmptyList() {
        URI link = URI.create("link");

        when(linkRepository.findByLink(link.toString())).thenReturn(Optional.empty());

        List<ProcessedIdDTO> allProcessedIds = linkService.findAllProcessedIds(link);

        assertNotNull(allProcessedIds);
        assertTrue(allProcessedIds.isEmpty());
    }

    @Test
    public void findAllProcessedIds_whenLinkExists_shouldReturnProcessedIdsByLink() {
        URI link = URI.create("link");
        LinkEntity linkEntity = new LinkEntity();
        Set<ProcessedIdEntity> processedIds = Set.of(
                new ProcessedIdEntity(1L, ProcessedIdType.GITHUB_PULL_REQUEST.type(), linkEntity),
                new ProcessedIdEntity(2L, ProcessedIdType.STACKOVERFLOW_COMMENT.type(), linkEntity),
                new ProcessedIdEntity(3L, ProcessedIdType.STACKOVERFLOW_ANSWER.type(), linkEntity),
                new ProcessedIdEntity(4L, ProcessedIdType.GITHUB_ISSUE.type(), linkEntity));
        linkEntity.processedIds(processedIds);
        List<ProcessedIdDTO> expected = List.of(
                new ProcessedIdDTO(1L, ProcessedIdType.GITHUB_PULL_REQUEST),
                new ProcessedIdDTO(2L, ProcessedIdType.STACKOVERFLOW_COMMENT),
                new ProcessedIdDTO(3L, ProcessedIdType.STACKOVERFLOW_ANSWER),
                new ProcessedIdDTO(4L, ProcessedIdType.GITHUB_ISSUE));

        when(linkRepository.findByLink(link.toString())).thenReturn(Optional.of(linkEntity));

        List<ProcessedIdDTO> allProcessedIds = linkService.findAllProcessedIds(link).stream()
                .sorted(Comparator.comparing(ProcessedIdDTO::id))
                .toList();

        assertNotNull(allProcessedIds);
        assertFalse(allProcessedIds.isEmpty());
        assertEquals(expected, allProcessedIds);
    }

    @Test
    public void saveProcessedIds_whenLinkIsNoExists_shouldNotSaveProcessedIds() {
        URI link = URI.create("link");

        when(linkRepository.findByLink(link.toString())).thenReturn(Optional.empty());

        linkService.saveProcessedIds(link, List.of());

        verify(linkRepository, times(0)).save(any());
        verify(processedIdRepository, times(0)).save(any());
    }

    @Test
    public void saveProcessedIds_whenLinkExists_shouldSaveProcessedIds() {
        URI link = URI.create("link");

        LinkEntity linkEntity = new LinkEntity();
        Set<ProcessedIdEntity> processedIds = new HashSet<>();
        processedIds.add(new ProcessedIdEntity(1L, ProcessedIdType.GITHUB_PULL_REQUEST.type(), linkEntity));
        linkEntity.processedIds(processedIds);
        when(linkRepository.findByLink(link.toString())).thenReturn(Optional.of(linkEntity));

        linkService.saveProcessedIds(link, List.of(new ProcessedIdDTO(1L, ProcessedIdType.GITHUB_PULL_REQUEST)));

        verify(linkRepository, times(1)).save(any());
        verify(processedIdRepository, times(1)).save(any());
    }

    @Test
    public void findAllLinksByForceCheckDelay_whenDurationIsTooLess_thenReturnEmptyStream() {
        OffsetDateTime fixed = OffsetDateTime.of(2025, 3, 25, 12, 0, 0, 0, ZoneOffset.UTC);
        Duration duration = Duration.ofHours(1);

        when(clock.instant()).thenReturn(fixed.toInstant());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(config.pageSize()).thenReturn(1);

        Pageable pageable = PageRequest.of(0, 1, Sort.by("updatedAt").descending());
        when(linkRepository.findLinkEntitiesByUpdatedAtBefore(fixed.minus(duration), pageable))
                .thenReturn(Page.empty());
        when(config.pageSize()).thenReturn(1);

        List<URI> allLinksByForceCheckDelay =
                linkService.findAllLinksByForceCheckDelay(duration).toList();
        assertNotNull(allLinksByForceCheckDelay);
        assertTrue(allLinksByForceCheckDelay.isEmpty());
        verify(linkRepository, times(1)).findLinkEntitiesByUpdatedAtBefore(fixed.minus(duration), pageable);
    }

    @Test
    public void findSubscribedChats_whenLinkSubscribed_shouldReturnSubscribedChats() {
        URI link = URI.create("link");

        LinkEntity linkEntity = new LinkEntity();
        linkEntity.chats(Set.of(new ChatEntity(1L)));
        when(linkRepository.findByLink(link.toString())).thenReturn(Optional.of(linkEntity));

        List<Long> subscribedChats = linkService.findSubscribedChats(link);
        assertNotNull(subscribedChats);
        assertFalse(subscribedChats.isEmpty());
    }

    @Test
    public void findSubscribedChats_whenLinkIsNotSubscribed_shouldReturnEmptyList() {
        URI link = URI.create("link");

        when(linkRepository.findByLink(link.toString())).thenReturn(Optional.empty());

        List<Long> subscribedChats = linkService.findSubscribedChats(link);
        assertNotNull(subscribedChats);
        assertTrue(subscribedChats.isEmpty());
    }
}

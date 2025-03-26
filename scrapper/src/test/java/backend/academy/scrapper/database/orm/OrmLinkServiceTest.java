package backend.academy.scrapper.database.orm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.database.orm.entity.ChatEntity;
import backend.academy.scrapper.database.orm.entity.FilterEntity;
import backend.academy.scrapper.database.orm.entity.LinkEntity;
import backend.academy.scrapper.database.orm.entity.ProcessedIdEntity;
import backend.academy.scrapper.database.orm.entity.TagEntity;
import backend.academy.scrapper.database.orm.repository.OrmChatRepository;
import backend.academy.scrapper.database.orm.repository.OrmFilterRepository;
import backend.academy.scrapper.database.orm.repository.OrmLinkRepository;
import backend.academy.scrapper.database.orm.repository.OrmTagRepository;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;

@ExtendWith(MockitoExtension.class)
public class OrmLinkServiceTest {

    @Mock
    private OrmLinkRepository ormLinkRepository;

    @Mock
    private OrmChatRepository ormChatRepository;

    @Mock
    private OrmTagRepository ormTagRepository;

    @Mock
    private OrmFilterRepository ormFilterRepository;

    @InjectMocks
    private OrmLinkService ormLinkService;

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

        List<LinkResponse> allByChatId = ormLinkService.findAllByChatId(id);
        assertNotNull(allByChatId);
        assertFalse(allByChatId.isEmpty());
        assertEquals(List.of(expected), allByChatId);
    }

    @Test
    public void findAllById_whenIdNotExist_returnEmptyList() {
        Long id = 1L;
        when(ormChatRepository.existsById(id)).thenReturn(false);

        List<LinkResponse> allByChatId = ormLinkService.findAllByChatId(id);
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
        when(ormLinkRepository.findByLink(request.link().toString()))
                .thenReturn(Optional.of(new LinkEntity(
                        1L,
                        "link",
                        OffsetDateTime.now(),
                        Set.of(new TagEntity("tag")),
                        Set.of(new FilterEntity("filter")),
                        Set.of(new ChatEntity(1L)),
                        Set.of(new ProcessedIdEntity()))));

        Optional<LinkResponse> response = ormLinkService.subscribe(id, request);
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
        when(ormLinkRepository.findByLink(request.link().toString())).thenReturn(Optional.empty());
        when(ormLinkRepository.save(any())).thenReturn(set.iterator().next());
        when(ormTagRepository.save(any())).thenReturn(new TagEntity(1L, "tag", new LinkEntity()));
        when(ormFilterRepository.save(any())).thenReturn(new FilterEntity(1L, "filter", new LinkEntity()));

        Optional<LinkResponse> response = ormLinkService.subscribe(id, request);
        assertNotNull(response);
        assertTrue(response.isPresent());
        assertEquals(expected, response.get());
        verify(ormLinkRepository, times(1)).save(any());
        verify(ormTagRepository, times(1)).save(any());
        verify(ormFilterRepository, times(1)).save(any());
    }

    @Test
    public void subscribe_whenChatNotExist_returnEmptyOptional() {
        Long id = 1L;
        AddLinkRequest request = new AddLinkRequest(URI.create("link"), Set.of("tag"), Set.of("filter"));

        when(ormChatRepository.findById(id)).thenReturn(Optional.empty());

        Optional<LinkResponse> response = ormLinkService.subscribe(id, request);
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    public void unsubscribe_whenChatNotExist_returnEmptyOptional() {
        Long id = 1L;
        RemoveLinkRequest request = new RemoveLinkRequest(URI.create("link"));

        when(ormChatRepository.findById(id)).thenReturn(Optional.empty());

        Optional<LinkResponse> unsubscribed = ormLinkService.unsubscribe(id, request);
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
        when(ormLinkRepository.findByLink(request.link().toString())).thenReturn(Optional.empty());

        Optional<LinkResponse> unsubscribed = ormLinkService.unsubscribe(id, request);
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
        when(ormLinkRepository.findByLink(request.link().toString())).thenReturn(Optional.of(linkEntity));

        Optional<LinkResponse> unsubscribed = ormLinkService.unsubscribe(id, request);
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
        when(ormLinkRepository.findByLink(request.link().toString())).thenReturn(Optional.of(linkEntity));

        Optional<LinkResponse> unsubscribed = ormLinkService.unsubscribe(id, request);
        assertNotNull(unsubscribed);
        assertTrue(unsubscribed.isPresent());
        assertEquals(expected, unsubscribed.get());
    }
}

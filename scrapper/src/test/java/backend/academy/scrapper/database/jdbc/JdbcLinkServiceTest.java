package backend.academy.scrapper.database.jdbc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.config.DatabaseConfig;
import backend.academy.scrapper.database.jdbc.model.Link;
import backend.academy.scrapper.database.jdbc.model.ProcessedId;
import backend.academy.scrapper.database.jdbc.repository.JdbcChatRepository;
import backend.academy.scrapper.database.jdbc.repository.JdbcLinkRepository;
import backend.academy.scrapper.database.jdbc.repository.JdbcLinkToChatRepository;
import backend.academy.scrapper.database.jdbc.repository.JdbcProcessedIdRepository;
import backend.academy.scrapper.enums.ProcessedIdType;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
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
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;

@ExtendWith(MockitoExtension.class)
public class JdbcLinkServiceTest {

    @Mock
    private Clock clock;

    @Mock
    private DatabaseConfig config;

    @Mock
    private JdbcChatRepository chatRepository;

    @Mock
    private JdbcLinkRepository linkRepository;

    @Mock
    private JdbcLinkToChatRepository linkToChatRepository;

    @Mock
    private JdbcProcessedIdRepository processedIdRepository;

    @InjectMocks
    private JdbcLinkService linkService;

    @Test
    public void testGetAllLinksSuccess() {
        Long id = 1L;
        List<Link> links =
                List.of(new Link(1L, URI.create("uri"), Set.of("tag"), Set.of("filter"), OffsetDateTime.now()));
        List<LinkResponse> response = List.of(new LinkResponse(1L, URI.create("uri"), Set.of("tag"), Set.of("filter")));

        when(chatRepository.isClient(id)).thenReturn(true);
        when(linkToChatRepository.findAllIdByChatId(id)).thenReturn(List.of(id));
        when(linkRepository.findAllLinks(List.of(id))).thenReturn(links);
        List<LinkResponse> byChatId = linkService.findAllByChatId(id);

        assertThat(!byChatId.isEmpty()).isTrue();
        assertThat(byChatId.size()).isEqualTo(links.size());
        assertThat(byChatId.getFirst()).isEqualTo(response.getFirst());
        verify(linkRepository).findAllLinks(List.of(id));
        verify(chatRepository).isClient(id);
        verify(linkToChatRepository).findAllIdByChatId(id);
    }

    @Test
    public void testGetAllLinksFailure() {
        Long id = 1L;
        List<Long> list = List.of(id);
        when(linkToChatRepository.findAllIdByChatId(id)).thenReturn(list);
        when(linkRepository.findAllLinks(list)).thenReturn(List.of());
        when(chatRepository.isClient(id)).thenReturn(true);

        List<LinkResponse> byChatId = linkService.findAllByChatId(id);

        assertThat(byChatId.isEmpty()).isTrue();
        verify(linkRepository).findAllLinks(list);
        verify(chatRepository).isClient(id);
        verify(linkToChatRepository).findAllIdByChatId(id);
    }

    @Test
    public void whenLinkIsNotRegistered_thenSaveLinkAndSubscribeAccount() {
        Long id = 123L;
        Long linkId = -1L;
        AddLinkRequest addLinkRequest = new AddLinkRequest(URI.create("link"), Set.of("tag"), Set.of("filter"));
        Link link = new Link(1L, URI.create("link"), Set.of("tag"), Set.of("filter"), OffsetDateTime.now());
        LinkResponse expected = new LinkResponse(link.id(), link.url(), link.tags(), link.filters());

        when(chatRepository.isClient(id)).thenReturn(true);
        when(linkRepository.findByLink(addLinkRequest.link().toString())).thenReturn(Optional.empty());
        when(linkRepository.save(addLinkRequest)).thenReturn(link);

        Optional<LinkResponse> linkResponse = linkService.subscribe(id, addLinkRequest);
        assertThat(linkResponse.isPresent()).isTrue();
        assertEquals(expected, linkResponse.get());
        verify(chatRepository).isClient(id);
        verify(linkRepository).findByLink(addLinkRequest.link().toString());
        verify(linkRepository).save(addLinkRequest);
        verify(linkToChatRepository).subscribeChatOnLink(id, link.id());
    }

    @Test
    public void whenLinkIsRegistered_thenOnlySubscribeAccount() {
        Long id = 123L;
        Long linkId = 1L;
        AddLinkRequest addLinkRequest = new AddLinkRequest(URI.create("link"), Set.of("tag"), Set.of("filter"));
        Link link = new Link(1L, URI.create("link"), Set.of("tag"), Set.of("filter"), OffsetDateTime.now());
        LinkResponse expected = new LinkResponse(link.id(), link.url(), link.tags(), link.filters());

        when(chatRepository.isClient(id)).thenReturn(true);
        when(linkRepository.findByLink(addLinkRequest.link().toString())).thenReturn(Optional.of(linkId));
        when(linkRepository.findById(linkId)).thenReturn(Optional.of(link));

        Optional<LinkResponse> linkResponse = linkService.subscribe(id, addLinkRequest);
        assertThat(linkResponse.isPresent()).isTrue();
        assertEquals(expected, linkResponse.get());
        verify(chatRepository).isClient(id);
        verify(linkRepository).findByLink(addLinkRequest.link().toString());
        verify(linkRepository).findById(linkId);
        verify(linkToChatRepository).subscribeChatOnLink(id, linkId);
    }

    @Test
    public void testUnsubscribeLinkSuccess() {
        Long id = 1L;
        RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest(URI.create("uri"));
        Link link = new Link(1L, URI.create("uri"), Set.of(), Set.of(), OffsetDateTime.now());
        LinkResponse linkResponse = new LinkResponse(1L, URI.create("uri"), Set.of(), Set.of());

        when(chatRepository.isClient(id)).thenReturn(true);
        when(linkRepository.existsLink(removeLinkRequest.link().toString())).thenReturn(true);
        when(linkRepository.delete(removeLinkRequest.link().toString())).thenReturn(Optional.of(link));
        Optional<LinkResponse> response = linkService.unsubscribe(id, removeLinkRequest);

        assertThat(response.isPresent()).isTrue();
        assertThat(response.get()).isEqualTo(linkResponse);
        verify(linkRepository).delete(removeLinkRequest.link().toString());
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
        Set<ProcessedId> processedIds = Set.of(
                new ProcessedId(1L, ProcessedIdType.GITHUB_PULL_REQUEST.type()),
                new ProcessedId(2L, ProcessedIdType.STACKOVERFLOW_COMMENT.type()),
                new ProcessedId(3L, ProcessedIdType.STACKOVERFLOW_ANSWER.type()),
                new ProcessedId(4L, ProcessedIdType.GITHUB_ISSUE.type()));
        List<ProcessedIdDTO> expected = List.of(
                new ProcessedIdDTO(1L, ProcessedIdType.GITHUB_PULL_REQUEST),
                new ProcessedIdDTO(2L, ProcessedIdType.STACKOVERFLOW_COMMENT),
                new ProcessedIdDTO(3L, ProcessedIdType.STACKOVERFLOW_ANSWER),
                new ProcessedIdDTO(4L, ProcessedIdType.GITHUB_ISSUE));

        when(linkRepository.findByLink(link.toString())).thenReturn(Optional.of(1L));
        when(processedIdRepository.findAll(1L)).thenReturn(processedIds);

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

        verify(processedIdRepository, times(0)).saveAll(anyLong(), any());
    }

    @Test
    public void saveProcessedIds_whenLinkExists_shouldSaveProcessedIds() {
        URI link = URI.create("link");

        when(linkRepository.findByLink(link.toString())).thenReturn(Optional.of(1L));

        linkService.saveProcessedIds(link, List.of());

        verify(processedIdRepository, times(1)).saveAll(anyLong(), any());
    }

    @Test
    public void findAllLinksByForceCheckDelay_whenDurationIsTooLess_thenReturnEmptyStream() {
        OffsetDateTime fixed = OffsetDateTime.of(2025, 3, 25, 12, 0, 0, 0, ZoneOffset.UTC);
        Duration duration = Duration.ofHours(1);

        when(clock.instant()).thenReturn(fixed.toInstant());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(config.pageSize()).thenReturn(1);

        when(linkRepository.findAllLinksByUpdatedAt(fixed.minus(duration), 0, 1))
                .thenReturn(new HashSet<>());
        when(config.pageSize()).thenReturn(1);

        List<URI> allLinksByForceCheckDelay =
                linkService.findAllLinksByForceCheckDelay(duration).toList();
        assertNotNull(allLinksByForceCheckDelay);
        assertTrue(allLinksByForceCheckDelay.isEmpty());
        verify(linkRepository, times(1)).findAllLinksByUpdatedAt(fixed.minus(duration), 0, 1);
    }

    @Test
    public void findAllLinksByForceCheckDelay_whenDurationIsAcceptable_thenReturnNotEmptyStream() {
        OffsetDateTime fixed = OffsetDateTime.of(2025, 3, 25, 12, 0, 0, 0, ZoneOffset.UTC);
        Duration duration = Duration.ofHours(1);

        when(clock.instant()).thenReturn(fixed.toInstant());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(config.pageSize()).thenReturn(1);

        when(linkRepository.findAllLinksByUpdatedAt(fixed.minus(duration), 0, 1))
                .thenReturn(Set.of(URI.create("link")));
        when(linkRepository.findAllLinksByUpdatedAt(fixed.minus(duration), 1, 1))
                .thenReturn(Set.of());
        when(config.pageSize()).thenReturn(1);

        List<URI> allLinksByForceCheckDelay =
                linkService.findAllLinksByForceCheckDelay(duration).toList();
        assertNotNull(allLinksByForceCheckDelay);
        assertFalse(allLinksByForceCheckDelay.isEmpty());
        verify(linkRepository).findAllLinksByUpdatedAt(fixed.minus(duration), 0, 1);
    }

    @Test
    public void findSubscribedChats_whenLinkSubscribed_shouldReturnSubscribedChats() {
        URI link = URI.create("link");

        when(linkRepository.findByLink(link.toString())).thenReturn(Optional.of(1L));
        when(linkToChatRepository.findAllByLinkId(1L)).thenReturn(List.of(1L));

        List<Long> subscribedChats = linkService.findSubscribedChats(link);
        assertNotNull(subscribedChats);
        assertFalse(subscribedChats.isEmpty());
        verify(linkToChatRepository, times(1)).findAllByLinkId(1L);
    }

    @Test
    public void findSubscribedChats_whenLinkIsNotSubscribed_shouldReturnEmptyList() {
        URI link = URI.create("link");

        when(linkRepository.findByLink(link.toString())).thenReturn(Optional.empty());

        List<Long> subscribedChats = linkService.findSubscribedChats(link);
        assertNotNull(subscribedChats);
        assertTrue(subscribedChats.isEmpty());
        verify(linkToChatRepository, times(0)).findAllByLinkId(1L);
    }
}

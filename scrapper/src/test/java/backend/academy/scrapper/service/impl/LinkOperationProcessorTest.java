package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.config.properties.DatabaseProperty;
import backend.academy.scrapper.db.DbCommonService;
import backend.academy.scrapper.db.impl.DbLinkServiceImpl;
import backend.academy.scrapper.db.model.Link;
import backend.academy.scrapper.db.model.ProcessedId;
import backend.academy.scrapper.enums.ProcessedIdType;
import backend.academy.scrapper.mapper.LinkResponseMapper;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import backend.academy.scrapper.service.ChatOperationProcessor;
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

@ExtendWith(MockitoExtension.class)
public class LinkOperationProcessorTest {

    @Mock
    private Clock clock;
    @Mock
    private LinkResponseMapper mapper;
    @Mock
    private DatabaseProperty config;
    @Mock
    private DbCommonService commonService;
    @Mock
    private DbLinkServiceImpl dbLinkService;
    @Mock
    private ChatOperationProcessor chatService;
    @InjectMocks
    private LinkOperationProcessorImpl linkOperationProcessor;

    @Test
    public void testGetAllLinksSuccess() {
        Long id = 1L;
        List<Link> links = List.of(
            new Link(1L, URI.create("uri"), Set.of("tag"), Set.of("filter"), OffsetDateTime.now())
        );
        List<LinkResponse> expectedResponse = List.of(
            new LinkResponse(1L, URI.create("uri"), Set.of("tag"), Set.of("filter"))
        );
        when(chatService.isClient(id)).thenReturn(true);
        when(commonService.findAllLinkIdsByChatId(id)).thenReturn(List.of(id));
        when(dbLinkService.findAllLinks(List.of(id))).thenReturn(links);
        when(mapper.toLinkResponse(any())).thenAnswer(invocation -> {
            Link argument = invocation.getArgument(0);
            return new LinkResponse(
                argument.id(),
                argument.url(),
                argument.tags(),
                argument.filters()
            );
        });

        List<LinkResponse> byChatId = linkOperationProcessor.findAllByChatId(id);

        assertThat(!byChatId.isEmpty()).isTrue();
        assertThat(byChatId.size()).isEqualTo(links.size());
        assertThat(byChatId.getFirst()).isEqualTo(expectedResponse.getFirst());
        verify(dbLinkService).findAllLinks(List.of(id));
        verify(chatService).isClient(id);
        verify(commonService).findAllLinkIdsByChatId(id);
    }

    @Test
    public void testGetAllLinksFailure() {
        Long id = 1L;
        List<Long> list = List.of(id);
        when(commonService.findAllLinkIdsByChatId(id)).thenReturn(list);
        when(dbLinkService.findAllLinks(list)).thenReturn(List.of());
        when(chatService.isClient(id)).thenReturn(true);

        List<LinkResponse> byChatId = linkOperationProcessor.findAllByChatId(id);

        assertThat(byChatId.isEmpty()).isTrue();
        verify(dbLinkService).findAllLinks(list);
        verify(chatService).isClient(id);
        verify(commonService).findAllLinkIdsByChatId(id);
    }

    @Test
    public void whenLinkIsNotRegistered_thenSaveLinkAndSubscribeAccount() {
        Long id = 123L;
        AddLinkRequest addLinkRequest = new AddLinkRequest(URI.create("link"), Set.of("tag"), Set.of("filter"));
        Link link = new Link(1L, URI.create("link"), Set.of("tag"), Set.of("filter"), OffsetDateTime.now());
        LinkResponse expected = new LinkResponse(link.id(), link.url(), link.tags(), link.filters());

        when(chatService.isClient(id)).thenReturn(true);
        when(dbLinkService.findByLink(addLinkRequest.link().toString())).thenReturn(Optional.empty());
        when(dbLinkService.saveLink(addLinkRequest)).thenReturn(link);
        when(mapper.toLinkResponse(any())).thenAnswer(invocation -> {
            Link argument = invocation.getArgument(0);
            return new LinkResponse(
                argument.id(),
                argument.url(),
                argument.tags(),
                argument.filters()
            );
        });

        Optional<LinkResponse> linkResponse = linkOperationProcessor.subscribe(id, addLinkRequest);
        assertTrue(linkResponse.isPresent());
        assertEquals(expected, linkResponse.get());
        verify(chatService).isClient(id);
        verify(dbLinkService).findByLink(addLinkRequest.link().toString());
        verify(dbLinkService).saveLink(addLinkRequest);
        verify(chatService).subscribeChatOnLink(id, link.id());
    }

    @Test
    public void whenLinkIsRegistered_thenOnlySubscribeAccount() {
        Long id = 123L;
        Long linkId = 1L;
        AddLinkRequest addLinkRequest = new AddLinkRequest(URI.create("link"), Set.of("tag"), Set.of("filter"));
        Link link = new Link(1L, URI.create("link"), Set.of("tag"), Set.of("filter"), OffsetDateTime.now());
        LinkResponse expected = new LinkResponse(link.id(), link.url(), link.tags(), link.filters());

        when(chatService.isClient(id)).thenReturn(true);
        when(dbLinkService.findByLink(addLinkRequest.link().toString())).thenReturn(Optional.of(link));
        when(mapper.toLinkResponse(any())).thenAnswer(invocation -> {
            Link argument = invocation.getArgument(0);
            return new LinkResponse(
                argument.id(),
                argument.url(),
                argument.tags(),
                argument.filters()
            );
        });

        Optional<LinkResponse> linkResponse = linkOperationProcessor.subscribe(id, addLinkRequest);
        assertTrue(linkResponse.isPresent());
        assertEquals(expected, linkResponse.get());
        verify(chatService).isClient(id);
        verify(dbLinkService).findByLink(addLinkRequest.link().toString());
        verify(chatService).subscribeChatOnLink(id, linkId);
    }

    @Test
    public void testUnsubscribeLinkSuccess() {
        Long id = 1L;
        RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest(URI.create("uri"));
        Link link = new Link(1L, URI.create("uri"), Set.of(), Set.of(), OffsetDateTime.now());
        LinkResponse linkResponse = new LinkResponse(1L, URI.create("uri"), Set.of(), Set.of());
        when(chatService.isClient(id)).thenReturn(true);
        when(dbLinkService.existsLink(removeLinkRequest.link().toString())).thenReturn(true);
        when(dbLinkService.findByLink(removeLinkRequest.link().toString())).thenReturn(Optional.of(link));
        when(dbLinkService.delete(removeLinkRequest.link().toString())).thenReturn(Optional.of(link));
        when(mapper.toLinkResponse(any())).thenAnswer(invocation -> {
            Link argument = invocation.getArgument(0);
            return new LinkResponse(
                argument.id(),
                argument.url(),
                argument.tags(),
                argument.filters()
            );
        });

        Optional<LinkResponse> response = linkOperationProcessor.unsubscribe(id, removeLinkRequest);

        assertTrue(response.isPresent());
        assertThat(response.get()).isEqualTo(linkResponse);
        verify(dbLinkService).delete(removeLinkRequest.link().toString());
    }

    @Test
    public void findAllProcessedIds_whenLinkIsNoExists_shouldReturnEmptyList() {
        URI link = URI.create("link");

        when(dbLinkService.findByLink(link.toString())).thenReturn(Optional.empty());

        List<ProcessedIdDTO> allProcessedIds = linkOperationProcessor.findAllProcessedIds(link);

        assertNotNull(allProcessedIds);
        assertTrue(allProcessedIds.isEmpty());
    }

    @Test
    public void findAllProcessedIds_whenLinkExists_shouldReturnProcessedIdsByLink() {
        Link link = new Link(1L, URI.create("link"), Set.of(), Set.of(), OffsetDateTime.MIN);
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

        when(dbLinkService.findByLink(link.url().toString())).thenReturn(Optional.of(link));
        when(commonService.findAllProcessedIdsByLinkId(1L)).thenReturn(processedIds);

        List<ProcessedIdDTO> allProcessedIds = linkOperationProcessor.findAllProcessedIds(link.url()).stream()
            .sorted(Comparator.comparing(ProcessedIdDTO::id))
            .toList();

        assertNotNull(allProcessedIds);
        assertFalse(allProcessedIds.isEmpty());
        assertEquals(expected, allProcessedIds);
    }

    @Test
    public void saveProcessedIds_whenLinkIsNoExists_shouldNotSaveProcessedIds() {
        URI link = URI.create("link");

        when(dbLinkService.findByLink(link.toString())).thenReturn(Optional.empty());

        linkOperationProcessor.saveProcessedIds(link, List.of());

        verify(commonService, times(0)).saveAllProcessedIdsByLinkId(anyLong(), any());
    }

    @Test
    public void saveProcessedIds_whenLinkExists_shouldSaveProcessedIds() {
        Link link = new Link(1L, URI.create("link"), Set.of(), Set.of(), OffsetDateTime.now());

        when(dbLinkService.findByLink(link.url().toString())).thenReturn(Optional.of(link));

        linkOperationProcessor.saveProcessedIds(link.url(), List.of());

        verify(commonService, times(1)).saveAllProcessedIdsByLinkId(anyLong(), any());
    }

    @Test
    public void findAllLinksByForceCheckDelay_whenDurationIsTooLess_thenReturnEmptyStream() {
        OffsetDateTime fixed = OffsetDateTime.of(2025, 3, 25, 12, 0, 0, 0, ZoneOffset.UTC);
        Duration duration = Duration.ofHours(1);

        when(clock.instant()).thenReturn(fixed.toInstant());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(config.pageSize()).thenReturn(1);

        when(dbLinkService.findAllLinksByUpdatedAt(fixed.minus(duration), 0, 1))
            .thenReturn(new HashSet<>());
        when(config.pageSize()).thenReturn(1);

        Set<URI> allLinksByForceCheckDelay = linkOperationProcessor.findAllLinksByForceCheckDelay(duration, 0);
        assertNotNull(allLinksByForceCheckDelay);
        assertTrue(allLinksByForceCheckDelay.isEmpty());
        verify(dbLinkService, times(1)).findAllLinksByUpdatedAt(fixed.minus(duration), 0, 1);
    }

    @Test
    public void findAllLinksByForceCheckDelay_whenDurationIsAcceptable_thenReturnNotEmptyStream() {
        OffsetDateTime fixed = OffsetDateTime.of(2025, 3, 25, 12, 0, 0, 0, ZoneOffset.UTC);
        Duration duration = Duration.ofHours(1);

        when(clock.instant()).thenReturn(fixed.toInstant());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(config.pageSize()).thenReturn(1);

        when(dbLinkService.findAllLinksByUpdatedAt(fixed.minus(duration), 0, 1))
            .thenReturn(Set.of(URI.create("link")));
        when(config.pageSize()).thenReturn(1);

        Set<URI> allLinksByForceCheckDelay = linkOperationProcessor.findAllLinksByForceCheckDelay(duration, 0);
        assertNotNull(allLinksByForceCheckDelay);
        assertFalse(allLinksByForceCheckDelay.isEmpty());
        verify(dbLinkService).findAllLinksByUpdatedAt(fixed.minus(duration), 0, 1);
    }

    @Test
    public void findSubscribedChats_whenLinkSubscribed_shouldReturnSubscribedChats() {
        Link link = new Link(1L, URI.create("link"), Set.of(), Set.of(), OffsetDateTime.now());

        when(dbLinkService.findByLink(link.url().toString())).thenReturn(Optional.of(link));
        when(chatService.findAllByLinkId(1L)).thenReturn(List.of(1L));

        List<Long> subscribedChats = linkOperationProcessor.findSubscribedChats(link.url());
        assertNotNull(subscribedChats);
        assertFalse(subscribedChats.isEmpty());
        verify(chatService, times(1)).findAllByLinkId(1L);
    }

    @Test
    public void findSubscribedChats_whenLinkIsNotSubscribed_shouldReturnEmptyList() {
        URI link = URI.create("link");

        when(dbLinkService.findByLink(link.toString())).thenReturn(Optional.empty());

        List<Long> subscribedChats = linkOperationProcessor.findSubscribedChats(link);
        assertNotNull(subscribedChats);
        assertTrue(subscribedChats.isEmpty());
        verify(chatService, times(0)).findAllByLinkId(1L);
    }
}

package backend.academy.scrapper.database.jdbc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.database.jdbc.model.Link;
import backend.academy.scrapper.database.jdbc.repository.JdbcChatRepository;
import backend.academy.scrapper.database.jdbc.repository.JdbcLinkRepository;
import backend.academy.scrapper.database.jdbc.repository.JdbcLinkToChatRepository;
import java.net.URI;
import java.time.OffsetDateTime;
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
    private JdbcLinkRepository linkRepository;

    @Mock
    private JdbcChatRepository chatRepository;

    @Mock
    private JdbcLinkToChatRepository linkToChatRepository;

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
}

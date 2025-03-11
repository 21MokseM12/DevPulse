package backend.academy.scrapper.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.ClientRepository;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;

@ExtendWith(MockitoExtension.class)
public class LinkServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private LinkService linkService;

    @BeforeEach
    void setUp() {
        linkService = new LinkService(clientRepository);
    }

    @Test
    public void testGetAllLinksSuccess() {
        Long id = 1L;
        List<Link> links =
                List.of(new Link(1L, URI.create("uri"), List.of("tag"), List.of("filter"), OffsetDateTime.now()));
        List<LinkResponse> response =
                List.of(new LinkResponse(1L, URI.create("uri"), List.of("tag"), List.of("filter")));

        when(clientRepository.findAllLinks(id)).thenReturn(links);
        Optional<List<LinkResponse>> byChatId = linkService.findAllByChatId(id);

        assertThat(byChatId.isPresent()).isTrue();
        assertThat(byChatId.get().size()).isEqualTo(links.size());
        assertThat(byChatId.get().get(0)).isEqualTo(response.get(0));
        verify(clientRepository).findAllLinks(id);
    }

    @Test
    public void testGetAllLinksFailure() {
        Long id = 1L;

        when(clientRepository.findAllLinks(id)).thenReturn(List.of());
        Optional<List<LinkResponse>> byChatId = linkService.findAllByChatId(id);

        assertThat(byChatId.isPresent()).isTrue();
        assertThat(byChatId.get().isEmpty()).isTrue();
        verify(clientRepository).findAllLinks(id);
    }

    @Test
    public void testSubscribeLinkSuccess() {
        Long id = 1L;
        AddLinkRequest addLinkRequest = new AddLinkRequest(URI.create("uri"), List.of(), List.of());
        Link link = new Link(1L, URI.create("uri"), List.of(), List.of(), OffsetDateTime.now());
        LinkResponse linkResponse = new LinkResponse(1L, URI.create("uri"), List.of(), List.of());

        when(clientRepository.saveLink(id, addLinkRequest)).thenReturn(link);
        Optional<LinkResponse> response = linkService.subscribe(id, addLinkRequest);

        assertThat(response.isPresent()).isTrue();
        assertThat(response.get()).isEqualTo(linkResponse);
        verify(clientRepository).saveLink(id, addLinkRequest);
    }

    @Test
    public void testUnsubscribeLinkSuccess() {
        Long id = 1L;
        RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest(URI.create("uri"));
        Link link = new Link(1L, URI.create("uri"), List.of(), List.of(), OffsetDateTime.now());
        LinkResponse linkResponse = new LinkResponse(1L, URI.create("uri"), List.of(), List.of());

        when(clientRepository.deleteLink(id, removeLinkRequest)).thenReturn(link);
        Optional<LinkResponse> response = linkService.unsubscribe(id, removeLinkRequest);

        assertThat(response.isPresent()).isTrue();
        assertThat(response.get()).isEqualTo(linkResponse);
        verify(clientRepository).deleteLink(id, removeLinkRequest);
    }
}

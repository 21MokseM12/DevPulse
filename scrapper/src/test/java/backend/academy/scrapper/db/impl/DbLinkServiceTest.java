package backend.academy.scrapper.db.impl;

import backend.academy.scrapper.db.model.Link;
import backend.academy.scrapper.db.repository.LinkRepository;
import backend.academy.scrapper.mapper.LinkMapper;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DbLinkServiceTest {

    @Mock
    private Clock clock;
    @Mock
    private LinkMapper linkMapper;
    @Mock
    private LinkRepository linkRepository;
    @InjectMocks
    private DbLinkServiceImpl dbLinkService;

    @Test
    public void saveLink_success() {
        AddLinkRequest request = new AddLinkRequest(URI.create("https://localhost"), Set.of("tag"), Set.of("filter"));
        Long linkId = 1L;
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(Instant.now());
        when(linkRepository.save(anyString(), any())).thenReturn(linkId);
        when(linkMapper.toLink(any(), anyLong(), any())).thenReturn(
            new Link(linkId, request.link(), request.tags(), request.filters(), OffsetDateTime.now())
        );

        Link response = dbLinkService.saveLink(request);

        assertEquals(linkId, response.id());
        verify(linkRepository, times(1)).save(anyString(), any());
    }

    @Test
    public void findByLink_success() {
        String url = "https://localhost";
        Link expected = new Link(1L, URI.create(url), Set.of(), Set.of(), OffsetDateTime.MIN);
        when(linkRepository.findIdByLink(url)).thenReturn(Optional.of(expected));

        Optional<Link> response = dbLinkService.findByLink(url);
        assertTrue(response.isPresent());
        assertEquals(expected, response.get());
    }

    @Test
    public void delete_success() {
        URI url = URI.create("https://localhost");
        Link expected = new Link(1L, url, Set.of(), Set.of(), OffsetDateTime.MIN);
        when(linkRepository.findIdByLink(url.toString())).thenReturn(Optional.of(expected));
        when(linkRepository.delete(expected.id())).thenReturn(Optional.of(expected));

        Optional<Link> response = dbLinkService.delete(url.toString());

        assertTrue(response.isPresent());
        assertEquals(expected, response.get());
        verify(linkRepository).delete(expected.id());
    }

    @Test
    public void delete_linkNotFound_shouldReturnOptionalEmpty() {
        URI url = URI.create("https://localhost");
        when(linkRepository.findIdByLink(url.toString())).thenReturn(Optional.empty());

        Optional<Link> response = dbLinkService.delete(url.toString());

        assertTrue(response.isEmpty());
        verify(linkRepository, never()).delete(anyLong());
    }

    @Test
    public void existsLink_throwsDataAccessException_shouldReturnFalse() {
        String url = "https://localhost";
        when(linkRepository.existsLink(url)).thenThrow(EmptyResultDataAccessException.class);

        boolean response = dbLinkService.existsLink(url);

        assertFalse(response);
    }

    @Test
    public void findAllLinks_success() {
        Link first = new Link(1L, URI.create("https://localhost/1"), Set.of(), Set.of(), OffsetDateTime.MIN);
        Link second = new Link(2L, URI.create("https://localhost/2"), Set.of(), Set.of(), OffsetDateTime.MIN);
        when(linkRepository.findById(1L)).thenReturn(Optional.of(first));
        when(linkRepository.findById(2L)).thenReturn(Optional.of(second));

        List<Link> response = dbLinkService.findAllLinks(List.of(1L, 2L));

        assertEquals(List.of(first, second), response);
    }
}

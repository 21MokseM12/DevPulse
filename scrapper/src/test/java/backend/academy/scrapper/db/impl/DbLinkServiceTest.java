package backend.academy.scrapper.db.impl;

import backend.academy.scrapper.db.model.Link;
import backend.academy.scrapper.db.repository.FilterRepository;
import backend.academy.scrapper.db.repository.LinkRepository;
import backend.academy.scrapper.db.repository.TagRepository;
import backend.academy.scrapper.mapper.LinkMapper;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DbLinkServiceTest {

    @Mock
    private Clock clock;
    @Mock
    private LinkMapper linkMapper;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private LinkRepository linkRepository;
    @Mock
    private FilterRepository filterRepository;
    @InjectMocks
    private DbLinkServiceImpl dbLinkService;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {
        Logger logWatcher = (Logger) LoggerFactory.getLogger(DbLinkServiceImpl.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logWatcher.addAppender(listAppender);
    }

    @Test
    public void saveLink_success() {
        AddLinkRequest request = new AddLinkRequest(
            URI.create("https://localhost"),
            Set.of("tag"),
            Set.of("filter")
        );
        Long linkId = 1L;
        Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        Mockito.when(clock.instant()).thenReturn(Instant.now());
        Mockito.when(linkRepository.save(anyString(), any())).thenReturn(linkId);
        Mockito.when(linkMapper.toLink(any(), anyLong(), any()))
            .thenAnswer(invocation -> {
                AddLinkRequest addLinkRequest = invocation.getArgument(0);
                return new Link(
                    invocation.getArgument(1),
                    addLinkRequest.link(),
                    addLinkRequest.tags(),
                    addLinkRequest.filters(),
                    invocation.getArgument(2)
                );
            });

        Link response = dbLinkService.saveLink(request);

        assertNotNull(response);
        assertEquals(linkId, response.id());
        assertEquals(request.link(), response.url());
        assertEquals(request.tags(), response.tags());
        assertEquals(request.filters(), response.filters());
    }

    @Test
    public void saveLink_throwsDataAccessExceptionWhenSaveLink_shouldLogAndThrow() {
        AddLinkRequest request = new AddLinkRequest(
            URI.create("https://localhost"),
            Set.of("tag"),
            Set.of("filter")
        );
        String logMessageExpected = "Ошибка при сохранении ссылки по запросу: "
            .concat(request.toString());
        Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        Mockito.when(clock.instant()).thenReturn(Instant.now());
        Mockito.when(linkRepository.save(anyString(), any()))
            .thenThrow(EmptyResultDataAccessException.class);

        assertThrows(DataAccessException.class, () -> dbLinkService.saveLink(request));

        List<ILoggingEvent> logList = listAppender.list.stream()
            .filter(log -> Level.WARN.equals(log.getLevel()))
            .filter(log -> log.getMessage().contains("Ошибка"))
            .toList();

        assertEquals(1, logList.size());
        assertEquals(logMessageExpected, logList.getFirst().getFormattedMessage());
    }

    @Test
    public void saveLink_throwsDataAccessExceptionWhenSaveTags_shouldLogAndThrow() {
        AddLinkRequest request = new AddLinkRequest(
            URI.create("https://localhost"),
            Set.of("tag"),
            Set.of("filter")
        );
        Long linkId = 1L;
        String logMessageExpected = "Ошибка при сохранении ссылки по запросу: "
            .concat(request.toString());
        Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        Mockito.when(clock.instant()).thenReturn(Instant.now());
        Mockito.when(linkRepository.save(anyString(), any())).thenReturn(linkId);
        Mockito.doThrow(EmptyResultDataAccessException.class)
            .when(tagRepository).save(request.tags(), linkId);

        assertThrows(DataAccessException.class, () -> dbLinkService.saveLink(request));

        List<ILoggingEvent> logList = listAppender.list.stream()
            .filter(log -> Level.WARN.equals(log.getLevel()))
            .filter(log -> log.getMessage().contains("Ошибка"))
            .toList();

        assertEquals(1, logList.size());
        assertEquals(logMessageExpected, logList.getFirst().getFormattedMessage());
    }

    @Test
    public void saveLink_throwsDataAccessExceptionWhenSaveFilters_shouldLogAndThrow() {
        AddLinkRequest request = new AddLinkRequest(
            URI.create("https://localhost"),
            Set.of("tag"),
            Set.of("filter")
        );
        Long linkId = 1L;
        String logMessageExpected = "Ошибка при сохранении ссылки по запросу: "
            .concat(request.toString());
        Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        Mockito.when(clock.instant()).thenReturn(Instant.now());
        Mockito.when(linkRepository.save(anyString(), any())).thenReturn(linkId);
        Mockito.doThrow(EmptyResultDataAccessException.class)
            .when(filterRepository).save(request.filters(), linkId);

        assertThrows(DataAccessException.class, () -> dbLinkService.saveLink(request));

        List<ILoggingEvent> logList = listAppender.list.stream()
            .filter(log -> Level.WARN.equals(log.getLevel()))
            .filter(log -> log.getMessage().contains("Ошибка"))
            .toList();

        assertEquals(1, logList.size());
        assertEquals(logMessageExpected, logList.getFirst().getFormattedMessage());
    }

    @Test
    public void findByLink_success() {
        String link = "https://localhost";
        Link expectedLink = new Link(
            1L,
            URI.create(link),
            Set.of("tag"),
            Set.of("filter"),
            OffsetDateTime.MIN
        );
        Mockito.when(linkRepository.findIdByLink(link)).thenReturn(Optional.of(expectedLink));

        var response = dbLinkService.findByLink(link);

        assertTrue(response.isPresent());
        assertEquals(expectedLink, response.get());
    }

    @Test
    public void findByLink_throwsDataAccessException_shouldLogAndReturnOptionalEmpty() {
        String link = "https://localhost";
        String expectedMessage = "Произошла ошибка при поиске ссылки {}: {}";
        Mockito.when(linkRepository.findIdByLink(link))
            .thenThrow(EmptyResultDataAccessException.class);

        var response = dbLinkService.findByLink(link);

        assertTrue(response.isEmpty());

        List<ILoggingEvent> logList = listAppender.list.stream()
            .filter(log -> Level.WARN.equals(log.getLevel()))
            .filter(log -> log.getMessage().contains("ошибка"))
            .toList();
        assertEquals(1, logList.size());
        assertEquals(expectedMessage, logList.getFirst().getMessage());
    }

    @Test
    public void findById_success() {
        Long id = 1L;
        Link expectedLink = new Link(
            id,
            URI.create("https://localhost"),
            Set.of("tag"),
            Set.of("filter"),
            OffsetDateTime.MIN
        );
        Mockito.when(linkRepository.findById(id)).thenReturn(Optional.of(expectedLink));

        var response = dbLinkService.findById(id);

        assertTrue(response.isPresent());
        assertEquals(expectedLink, response.get());
    }

    @Test
    public void findById_throwsDataAccessException_shouldLogAndReturnOptionalEmpty() {
        Long id = 1L;
        String expectedMessage = "Произошла ошибка при поиске ссылки по id {}: {}";
        Mockito.when(linkRepository.findById(id))
            .thenThrow(EmptyResultDataAccessException.class);

        var response = dbLinkService.findById(id);

        assertTrue(response.isEmpty());

        List<ILoggingEvent> logList = listAppender.list.stream()
            .filter(log -> Level.WARN.equals(log.getLevel()))
            .filter(log -> log.getMessage().contains("ошибка"))
            .toList();
        assertEquals(1, logList.size());
        assertEquals(expectedMessage, logList.getFirst().getMessage());
    }

    @Test
    public void existsLink_success() {
        String link = "https://localhost";
        Mockito.when(linkRepository.existsLink(link)).thenReturn(true);

        var response = dbLinkService.existsLink(link);

        assertTrue(response);
    }

    @Test
    public void existsLink_throwsDataAccessException_shouldLogAndReturnFalse() {
        String link = "https://localhost";
        String expectedMessage = "Произошла ошибка при проверке существования ссылки: "
            .concat(link);
        Mockito.when(linkRepository.existsLink(link))
            .thenThrow(EmptyResultDataAccessException.class);

        var response = dbLinkService.existsLink(link);

        assertFalse(response);

        List<ILoggingEvent> logList = listAppender.list.stream()
            .filter(log -> Level.WARN.equals(log.getLevel()))
            .filter(log -> log.getMessage().contains("ошибка"))
            .toList();
        assertEquals(1, logList.size());
        assertEquals(expectedMessage, logList.getFirst().getFormattedMessage());
    }

    @Test
    public void delete_success() {
        Link expectedLink = new Link(
            1L,
            URI.create("https://localhost"),
            Set.of("tag"),
            Set.of("filter"),
            OffsetDateTime.MIN
        );
        Mockito.when(linkRepository.findIdByLink(expectedLink.url().toString()))
            .thenReturn(Optional.of(expectedLink));
        Mockito.when(tagRepository.deleteByLinkId(expectedLink.id()))
            .thenReturn(expectedLink.tags());
        Mockito.when(filterRepository.deleteByLinkId(expectedLink.id()))
            .thenReturn(expectedLink.filters());
        Mockito.when(linkRepository.delete(expectedLink.id()))
            .thenReturn(Optional.of(expectedLink));
        Mockito.when(linkMapper.toLink(any(Link.class), any(), any()))
            .thenReturn(expectedLink);

        var response = dbLinkService.delete(expectedLink.url().toString());

        assertTrue(response.isPresent());
        assertEquals(expectedLink, response.get());
        verify(linkRepository, times(1)).findIdByLink(expectedLink.url().toString());
        verify(tagRepository, times(1)).deleteByLinkId(expectedLink.id());
        verify(filterRepository, times(1)).deleteByLinkId(expectedLink.id());
        verify(linkRepository, times(1)).delete(expectedLink.id());
    }

    @Test
    public void delete_linkNotFound_shouldReturnOptionalEmpty() {
        URI link = URI.create("https://localhost");
        Mockito.when(linkRepository.findIdByLink(link.toString()))
            .thenReturn(Optional.empty());

        var response = dbLinkService.delete(link.toString());

        assertTrue(response.isEmpty());
        verify(linkRepository, times(1)).findIdByLink(link.toString());
        verify(tagRepository, never()).deleteByLinkId(any());
        verify(filterRepository, never()).deleteByLinkId(any());
        verify(linkRepository, never()).delete(any());
    }

    @Test
    public void delete_throwsDataAccessException_shouldLogAndReturnOptionalEmpty() {
        URI link = URI.create("https://localhost");
        String expectedMessage = "Произошла ошибка при удалении ссылки {}: {}";
        Mockito.when(linkRepository.findIdByLink(link.toString()))
            .thenThrow(EmptyResultDataAccessException.class);

        var response = dbLinkService.delete(link.toString());

        assertTrue(response.isEmpty());

        List<ILoggingEvent> logList = listAppender.list.stream()
            .filter(log -> Level.WARN.equals(log.getLevel()))
            .filter(log -> log.getMessage().contains("ошибка"))
            .toList();
        assertEquals(1, logList.size());
        assertEquals(expectedMessage, logList.getFirst().getMessage());
    }

    @Test
    public void findAllLinks_success() {
        List<Link> expectedLinks = List.of(
            new Link(
                1L,
                URI.create("https://localhost"),
                Set.of("tag"),
                Set.of("filter"),
                OffsetDateTime.MIN
            ),
            new Link(
                2L,
                URI.create("https://localhost2"),
                Set.of("tag"),
                Set.of("filter"),
                OffsetDateTime.MIN
            ),
            new Link(
                3L,
                URI.create("https://localhost3"),
                Set.of("tag"),
                Set.of("filter"),
                OffsetDateTime.MIN
            )
        );
        expectedLinks.forEach(link ->
            Mockito.when(linkRepository.findById(link.id()))
                .thenReturn(Optional.of(link))
        );
        var request = expectedLinks.stream().map(Link::id).toList();

        var response = dbLinkService.findAllLinks(request);

        assertEquals(expectedLinks, response);
    }

    @Test
    public void findAllLInks_emptyIdList_shouldReturnEmptyList() {
        var response = dbLinkService.findAllLinks(List.of());
        var expectedMessage = "Переданный список id ссылок пуст";

        assertTrue(response.isEmpty());

        List<ILoggingEvent> logList = listAppender.list.stream()
            .filter(log -> Level.WARN.equals(log.getLevel()))
            .filter(log -> log.getMessage().equals(expectedMessage))
            .toList();
        assertEquals(1, logList.size());
        assertEquals(expectedMessage, logList.getFirst().getMessage());
    }

    @Test
    public void findAllLinks_allLinksNotExists_shouldReturnEmptyList() {
        var request = List.of(1L, 2L, 3L);
        request.forEach(id ->
            Mockito.when(linkRepository.findById(id))
                .thenReturn(Optional.empty())
        );

        var response = dbLinkService.findAllLinks(request);

        assertTrue(response.isEmpty());
    }

    @Test
    public void findAllLinks_partOfLinksExists_shouldReturnListOfLinks() {
        var request = List.of(1L, 2L, 3L);
        var expectedLinks = List.of(
            new Link(
                1L,
                URI.create("https://localhost"),
                Set.of("tag"),
                Set.of("filter"),
                OffsetDateTime.MIN
            )
        );
        Mockito.when(linkRepository.findById(1L))
            .thenReturn(Optional.of(expectedLinks.getFirst()));
        Mockito.when(linkRepository.findById(2L)).thenReturn(Optional.empty());
        Mockito.when(linkRepository.findById(3L)).thenReturn(Optional.empty());

        var response = dbLinkService.findAllLinks(request);

        assertEquals(expectedLinks, response);
    }

    @Test
    public void findAllLinksByUpdatedAt_success() {
        var highestTimeLimit = OffsetDateTime.MAX;
        var offset = 1;
        var limit = 10;
        Set<URI> expected = Set.of(
            URI.create("https://localhost"),
            URI.create("https://localhost2")
        );
        Mockito.when(linkRepository.findAllLinksByUpdatedAt(highestTimeLimit, offset * limit, limit))
            .thenReturn(expected);

        var response = dbLinkService.findAllLinksByUpdatedAt(highestTimeLimit, offset, limit);

        assertEquals(expected, response);
    }

    @Test
    public void findAllLinksByUpdatedAt_throwsDataAccessException_shouldLogAndReturnEmptySet() {
        var highestTimeLimit = OffsetDateTime.MAX;
        var offset = 1;
        var limit = 10;
        var expectedMessage = "Произошла ошибка при поиске ссылок по дате обновления: {}";
        Mockito.when(linkRepository.findAllLinksByUpdatedAt(highestTimeLimit, offset * limit, limit))
            .thenThrow(EmptyResultDataAccessException.class);

        var response = dbLinkService.findAllLinksByUpdatedAt(highestTimeLimit, offset, limit);

        assertTrue(response.isEmpty());

        List<ILoggingEvent> logList = listAppender.list.stream()
            .filter(log -> Level.WARN.equals(log.getLevel()))
            .filter(log -> log.getMessage().contains("ошибка"))
            .toList();
        assertEquals(1, logList.size());
        assertEquals(expectedMessage, logList.getFirst().getMessage());
    }
}

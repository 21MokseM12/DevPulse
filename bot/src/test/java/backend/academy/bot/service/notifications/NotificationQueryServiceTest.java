package backend.academy.bot.service.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.db.model.NotificationView;
import backend.academy.bot.db.repository.NotificationRepository;
import backend.academy.bot.model.api.MarkReadRequest;
import backend.academy.bot.model.api.MarkReadResponse;
import backend.academy.bot.model.api.NotificationListResponse;
import backend.academy.bot.service.ScrapperConnectionService;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.response.LinkResponse;

class NotificationQueryServiceTest {

    private NotificationRepository notificationRepository;
    private ScrapperConnectionService scrapperConnectionService;
    private NotificationQueryService service;

    @BeforeEach
    void setUp() {
        notificationRepository = Mockito.mock(NotificationRepository.class);
        scrapperConnectionService = Mockito.mock(ScrapperConnectionService.class);
        service = new NotificationQueryService(notificationRepository, scrapperConnectionService);
    }

    @Test
    void list_withoutTags_readsByClientLogin() throws Exception {
        NotificationView row = new NotificationView(
                1L,
                10L,
                "https://github.com/org/repo/issues/1",
                "https://github.com/org/repo/issues/1",
                "title",
                "owner",
                "description",
                OffsetDateTime.parse("2026-04-26T00:00:00Z"),
                OffsetDateTime.parse("2026-04-26T00:01:00Z"),
                null);
        when(notificationRepository.findByClientLogin("1", 20, 0)).thenReturn(List.of(row));

        NotificationListResponse response = service.list("1", null, null, null);

        assertEquals(1, response.notifications().size());
        assertEquals(true, response.notifications().getFirst().unread());
        verify(scrapperConnectionService, never()).getAllLinks(any(String.class));
    }

    @Test
    void list_withTags_filtersByScrapperTags() throws Exception {
        when(scrapperConnectionService.getAllLinks("1"))
                .thenReturn(List.of(
                        new LinkResponse(
                                10L,
                                URI.create("https://github.com/org/repo/issues/1"),
                                Set.of("backend", "java"),
                                Set.of()),
                        new LinkResponse(
                                11L, URI.create("https://github.com/org/repo/issues/2"), Set.of("mobile"), Set.of())));

        service.list("1", 10, 0, Set.of("backend"));

        verify(notificationRepository)
                .findByClientLoginAndUrls("1", Set.of("https://github.com/org/repo/issues/1"), 10, 0);
    }

    @Test
    void markRead_whenIdsMissingAndMarkAllFalse_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> service.markRead("1", new MarkReadRequest(Set.of(), false)));
    }

    @Test
    void markRead_whenRepeatedIds_returnsIdempotentUpdateCount() throws Exception {
        when(notificationRepository.markAsReadByIds(eq("1"), eq(Set.of(1L, 2L)), any()))
                .thenReturn(0L);

        MarkReadResponse response = service.markRead("1", new MarkReadRequest(Set.of(1L, 2L), false));

        assertEquals(0, response.updatedCount());
    }
}

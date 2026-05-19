package backend.academy.bot.service.notifications;

import backend.academy.bot.db.model.NotificationView;
import backend.academy.bot.db.repository.NotificationRepository;
import backend.academy.bot.model.api.MarkReadRequest;
import backend.academy.bot.model.api.MarkReadResponse;
import backend.academy.bot.model.api.NotificationDto;
import backend.academy.bot.model.api.NotificationListResponse;
import backend.academy.bot.model.api.UnreadCountResponse;
import backend.academy.bot.service.ScrapperConnectionService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.exceptions.BadRequestException;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final NotificationRepository notificationRepository;
    private final ScrapperConnectionService scrapperConnectionService;

    public NotificationListResponse list(String clientLogin, Integer limit, Integer offset, Set<String> tags)
            throws BadRequestException {
        int resolvedLimit = normalizeLimit(limit);
        int resolvedOffset = normalizeOffset(offset);
        Set<String> normalizedTags = normalizeTags(tags);

        List<NotificationView> notifications;
        if (normalizedTags.isEmpty()) {
            notifications = notificationRepository.findByClientLogin(clientLogin, resolvedLimit, resolvedOffset);
        } else {
            Set<String> filteredUrls = scrapperConnectionService.getAllLinks(clientLogin).stream()
                    .filter(link -> link.tags() != null && link.tags().containsAll(normalizedTags))
                    .map(link -> link.url().toString())
                    .collect(Collectors.toSet());
            notifications = notificationRepository.findByClientLoginAndUrls(
                    clientLogin, filteredUrls, resolvedLimit, resolvedOffset);
        }

        List<NotificationDto> result = notifications.stream().map(this::toDto).toList();
        return new NotificationListResponse(result, resolvedLimit, resolvedOffset);
    }

    public UnreadCountResponse unreadCount(String clientLogin) {
        return new UnreadCountResponse(notificationRepository.countUnreadByClientLogin(clientLogin));
    }

    public MarkReadResponse markRead(String clientLogin, MarkReadRequest request) throws BadRequestException {
        Set<Long> ids = request == null || request.ids() == null ? Set.of() : request.ids();
        boolean markAll = request != null && Boolean.TRUE.equals(request.markAll());
        if (!markAll && ids.isEmpty()) {
            throw new BadRequestException("Для mark read передайте ids или markAll=true");
        }

        OffsetDateTime readAt = OffsetDateTime.now(ZoneOffset.UTC);
        long updated = markAll
                ? notificationRepository.markAllAsRead(clientLogin, readAt)
                : notificationRepository.markAsReadByIds(clientLogin, ids, readAt);
        return new MarkReadResponse(updated);
    }

    private int normalizeLimit(Integer limit) throws BadRequestException {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new BadRequestException("Параметр limit должен быть в диапазоне 1..100");
        }
        return limit;
    }

    private int normalizeOffset(Integer offset) throws BadRequestException {
        if (offset == null) {
            return 0;
        }
        if (offset < 0) {
            throw new BadRequestException("Параметр offset должен быть >= 0");
        }
        return offset;
    }

    private Set<String> normalizeTags(Set<String> tags) {
        if (tags == null) {
            return Set.of();
        }
        return tags.stream().filter(tag -> tag != null && !tag.isBlank()).collect(Collectors.toSet());
    }

    private NotificationDto toDto(NotificationView row) {
        return new NotificationDto(
                row.id(),
                row.linkId(),
                row.url(),
                row.eventUrl(),
                row.title(),
                row.updateOwner(),
                row.description(),
                row.creationDate(),
                row.receivedAt(),
                row.readAt(),
                row.readAt() == null);
    }
}

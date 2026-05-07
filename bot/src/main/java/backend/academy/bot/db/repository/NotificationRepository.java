package backend.academy.bot.db.repository;

import backend.academy.bot.db.model.Notification;
import backend.academy.bot.db.model.NotificationView;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

public interface NotificationRepository {
    long save(Notification notification);

    List<NotificationView> findByClientLogin(String clientLogin, int limit, int offset);

    List<NotificationView> findByClientLoginAndUrls(String clientLogin, Set<String> urls, int limit, int offset);

    long countUnreadByClientLogin(String clientLogin);

    long markAsReadByIds(String clientLogin, Set<Long> notificationIds, OffsetDateTime readAt);

    long markAllAsRead(String clientLogin, OffsetDateTime readAt);
}

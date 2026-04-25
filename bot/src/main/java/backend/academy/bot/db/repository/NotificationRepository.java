package backend.academy.bot.db.repository;

import backend.academy.bot.db.model.Notification;

public interface NotificationRepository {
    long save(Notification notification);
}

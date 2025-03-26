package backend.academy.scrapper.service.notifications;

import backend.academy.scrapper.model.NotifyUpdateEntity;
import java.util.List;

public interface NotificationManager {
    void notify(List<NotifyUpdateEntity> notifications);
}

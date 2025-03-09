package backend.academy.scrapper.service.notifications;

import java.net.URI;
import java.util.List;
import java.util.Map;

public interface NotificationManager {
    void notify(Map<URI, List<Long>> notificationsMap);
}

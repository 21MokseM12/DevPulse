package backend.academy.bot.service.notifications;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.db.model.Notification;
import backend.academy.bot.db.repository.NotificationRepository;
import backend.academy.bot.mapper.LinkUpdateNotificationMapper;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import scrapper.bot.connectivity.model.LinkUpdate;

@SpringJUnitConfig(BotNotificationManager.class)
class BotNotificationManagerTest {

    @MockitoBean
    private NotificationRepository notificationRepository;

    @MockitoBean
    private LinkUpdateNotificationMapper notificationMapper;

    @Autowired
    private BotNotificationManager manager;

    @Test
    void notify_savesIncomingUpdate() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-04-25T18:30:00Z");
        LinkUpdate update = new LinkUpdate(
                100L,
                URI.create("https://github.com/org/repo/pull/123"),
                "PR updated",
                "octocat",
                "description",
                createdAt,
                List.of(10L, 20L));
        Notification notification = new Notification(
                100L,
                "https://github.com/org/repo/pull/123",
                "PR updated",
                "octocat",
                "description",
                createdAt,
                List.of(10L, 20L));
        when(notificationMapper.map(update)).thenReturn(notification);

        manager.notify(update);

        verify(notificationMapper).map(update);
        verify(notificationRepository).save(notification);
    }
}

package backend.academy.scrapper.service.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.properties.DatabaseProperty;
import backend.academy.scrapper.factory.LinkUpdaterServiceFactory;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.NotifyUpdateEntity;
import backend.academy.scrapper.model.UpdateType;
import backend.academy.scrapper.service.LinkOperationProcessor;
import backend.academy.scrapper.service.notifications.NotificationManager;
import backend.academy.scrapper.service.updaters.LinkUpdater;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkUpdateScheduledListenerTest {

    @Mock
    private DatabaseProperty databaseProperty;

    @Mock
    private LinkUpdaterServiceFactory updaterFactory;

    @Mock
    private LinkOperationProcessor linkOperationProcessor;

    @Mock
    private NotificationManager notificationManager;

    @Mock
    private LinkUpdater linkUpdater;

    @Test
    void listenUpdates_whenPollSuccess_marksPollingSuccess() {
        ScrapperConfig config = buildConfig();
        LinkUpdateScheduledListener listener = new LinkUpdateScheduledListener(
                config, databaseProperty, updaterFactory, linkOperationProcessor, notificationManager);
        listener.init();

        URI link = URI.create("https://github.com/acme/repo");
        when(databaseProperty.pageSize()).thenReturn(1000);
        when(linkOperationProcessor.findAllLinksByForceCheckDelay(
                        config.scheduler().forceCheckDelay(), 0))
                .thenReturn(Set.of(link));
        when(linkOperationProcessor.findAllLinksByForceCheckDelay(
                        config.scheduler().forceCheckDelay(), 1))
                .thenReturn(Set.of());
        when(updaterFactory.get(link)).thenReturn(linkUpdater);
        when(linkUpdater.getUpdates(link)).thenReturn(List.<LinkUpdateDTO>of());

        listener.listenUpdates();

        verify(linkOperationProcessor, times(1))
                .markPollingSuccess(eq(link), any(), eq(config.scheduler().forceCheckDelay()));
        verify(linkOperationProcessor, times(0)).markPollingFailure(any(), any(), any(), any());
    }

    @Test
    void listenUpdates_whenPollFails_marksPollingFailure() {
        ScrapperConfig config = buildConfig();
        LinkUpdateScheduledListener listener = new LinkUpdateScheduledListener(
                config, databaseProperty, updaterFactory, linkOperationProcessor, notificationManager);
        listener.init();

        URI link = URI.create("https://github.com/acme/repo");
        when(databaseProperty.pageSize()).thenReturn(1000);
        when(linkOperationProcessor.findAllLinksByForceCheckDelay(
                        config.scheduler().forceCheckDelay(), 0))
                .thenReturn(Set.of(link));
        when(linkOperationProcessor.findAllLinksByForceCheckDelay(
                        config.scheduler().forceCheckDelay(), 1))
                .thenReturn(Set.of());
        when(updaterFactory.get(link)).thenReturn(linkUpdater);
        when(linkUpdater.getUpdates(link)).thenThrow(new RuntimeException("upstream 503"));

        listener.listenUpdates();

        verify(linkOperationProcessor, times(1))
                .markPollingFailure(
                        eq(link),
                        any(),
                        eq(config.scheduler().forceCheckDelay()),
                        eq("RuntimeException: upstream 503"));
    }

    @Test
    void listenUpdates_whenSubscribersResolved_sendsNotificationWithClientLogins() {
        ScrapperConfig config = buildConfig();
        LinkUpdateScheduledListener listener = new LinkUpdateScheduledListener(
                config, databaseProperty, updaterFactory, linkOperationProcessor, notificationManager);
        listener.init();

        URI link = URI.create("https://github.com/acme/repo");
        LinkUpdateDTO update = new LinkUpdateDTO(
                10L, "title", "owner", OffsetDateTime.now(), "desc", UpdateType.GITHUB_COMMIT, Set.of());
        when(databaseProperty.pageSize()).thenReturn(1000);
        when(linkOperationProcessor.findAllLinksByForceCheckDelay(
                        config.scheduler().forceCheckDelay(), 0))
                .thenReturn(Set.of(link));
        when(linkOperationProcessor.findAllLinksByForceCheckDelay(
                        config.scheduler().forceCheckDelay(), 1))
                .thenReturn(Set.of());
        when(updaterFactory.get(link)).thenReturn(linkUpdater);
        when(linkUpdater.getUpdates(link)).thenReturn(List.of(update));
        when(linkOperationProcessor.findSubscribedChats(link, update)).thenReturn(List.of(1L, 2L));
        when(linkOperationProcessor.findClientLogins(List.of(1L, 2L))).thenReturn(List.of("alice", "bob"));
        when(notificationManager.notify(any()))
                .thenReturn(List.of(new NotifyUpdateEntity(link, List.of(update), List.of("alice", "bob"))));

        listener.listenUpdates();

        verify(notificationManager)
                .notify(org.mockito.ArgumentMatchers.argThat(notifications -> notifications.size() == 1
                        && notifications.getFirst().clientLogins().equals(List.of("alice", "bob"))));
        verify(linkOperationProcessor, times(1))
                .saveProcessedIds(eq(link), org.mockito.ArgumentMatchers.argThat(ids -> ids.size() == 1));
    }

    private ScrapperConfig buildConfig() {
        return new ScrapperConfig(
                new ScrapperConfig.GitHubCredentials("token", "https://api.github.com"),
                new ScrapperConfig.StackOverflowCredentials("https://api.stackexchange.com", "k", "t"),
                "http://localhost:8080",
                new ScrapperConfig.SchedulerCredentials(Duration.ofSeconds(15), Duration.ofSeconds(30), 1),
                new ScrapperConfig.OutboxCredentials("link-updates", Duration.ofSeconds(5), 100),
                new ScrapperConfig.DeliveryCredentials(ScrapperConfig.DeliveryMode.HTTP),
                new ScrapperConfig.AuthCredentials("X-Internal-Secret", "secret"));
    }
}

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
import backend.academy.scrapper.service.LinkOperationProcessor;
import backend.academy.scrapper.service.notifications.impl.ScrapperHttpNotificationManager;
import backend.academy.scrapper.service.updaters.LinkUpdater;
import java.net.URI;
import java.time.Duration;
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
    private ScrapperHttpNotificationManager notificationManager;

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
                .markPollingFailure(eq(link), any(), eq(config.scheduler().forceCheckDelay()), eq("upstream 503"));
    }

    private ScrapperConfig buildConfig() {
        return new ScrapperConfig(
                new ScrapperConfig.GitHubCredentials("token", "https://api.github.com"),
                new ScrapperConfig.StackOverflowCredentials("https://api.stackexchange.com", "k", "t"),
                "http://localhost:8080",
                new ScrapperConfig.SchedulerCredentials(Duration.ofSeconds(15), Duration.ofSeconds(30), 1),
                new ScrapperConfig.OutboxCredentials("link-updates", Duration.ofSeconds(5), 100),
                new ScrapperConfig.AuthCredentials("X-Internal-Secret", "secret"));
    }
}

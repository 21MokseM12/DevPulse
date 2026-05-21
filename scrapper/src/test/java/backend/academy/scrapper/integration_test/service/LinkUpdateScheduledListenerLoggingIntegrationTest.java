package backend.academy.scrapper.integration_test.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.properties.DatabaseProperty;
import backend.academy.scrapper.factory.LinkUpdaterServiceFactory;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.service.LinkOperationProcessor;
import backend.academy.scrapper.service.listeners.LinkUpdateScheduledListener;
import backend.academy.scrapper.service.notifications.NotificationManager;
import backend.academy.scrapper.service.updaters.LinkUpdater;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import scrapper.bot.connectivity.enums.LinkUpdaterType;

@SpringJUnitConfig(classes = LinkUpdateScheduledListenerLoggingIntegrationTest.TestConfig.class)
class LinkUpdateScheduledListenerLoggingIntegrationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private LinkUpdateScheduledListener listener;

    @org.springframework.beans.factory.annotation.Autowired
    private DatabaseProperty databaseProperty;

    @org.springframework.beans.factory.annotation.Autowired
    private LinkUpdaterServiceFactory updaterFactory;

    @org.springframework.beans.factory.annotation.Autowired
    private LinkUpdater linkUpdater;

    @org.springframework.beans.factory.annotation.Autowired
    private LinkOperationProcessor linkOperationProcessor;

    @Test
    void listenUpdates_whenNoEventsFound_writesHumanReadableNoUpdatesLog() {
        URI link = URI.create("https://stackoverflow.com/questions/123");
        when(databaseProperty.pageSize()).thenReturn(1000);
        when(linkOperationProcessor.findAllLinksByForceCheckDelay(Duration.ofSeconds(30), 0))
                .thenReturn(Set.of(link));
        when(linkOperationProcessor.findAllLinksByForceCheckDelay(Duration.ofSeconds(30), 1))
                .thenReturn(Set.of());
        when(updaterFactory.get(link)).thenReturn(linkUpdater);
        when(linkUpdater.getType()).thenReturn(LinkUpdaterType.STACK_OVERFLOW);
        when(linkUpdater.getUpdates(link)).thenReturn(List.<LinkUpdateDTO>of());

        Logger logger = (Logger) LoggerFactory.getLogger(LinkUpdateScheduledListener.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        logger.addAppender(appender);
        appender.start();
        try {
            listener.listenUpdates();
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }

        List<String> messages =
                appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
        assertThat(messages)
                .anyMatch(message -> message.contains("Начинается просмотр по ссылке: " + link))
                .anyMatch(message -> message.contains("Ответ от STACK_OVERFLOW получен успешно"))
                .anyMatch(message -> message.contains("Новых событий не обнаружено по ссылке: " + link))
                .anyMatch(
                        message -> message.contains("Просмотр завершен по ссылке: " + link + " (результат=no_updates"));
    }

    @Configuration
    static class TestConfig {
        @Bean
        ScrapperConfig scrapperConfig() {
            return new ScrapperConfig(
                    new ScrapperConfig.GitHubCredentials("token", "https://api.github.com"),
                    new ScrapperConfig.StackOverflowCredentials("https://api.stackexchange.com", "k", "t"),
                    "http://localhost:8080",
                    new ScrapperConfig.SchedulerCredentials(Duration.ofSeconds(15), Duration.ofSeconds(30), 1),
                    new ScrapperConfig.OutboxCredentials("link-updates", Duration.ofSeconds(5), 100),
                    new ScrapperConfig.DeliveryCredentials(ScrapperConfig.DeliveryMode.HTTP),
                    new ScrapperConfig.AuthCredentials("X-Internal-Secret", "secret"));
        }

        @Bean
        DatabaseProperty databaseProperty() {
            return Mockito.mock(DatabaseProperty.class);
        }

        @Bean
        LinkUpdaterServiceFactory updaterFactory() {
            return Mockito.mock(LinkUpdaterServiceFactory.class);
        }

        @Bean
        LinkUpdater linkUpdater() {
            return Mockito.mock(LinkUpdater.class);
        }

        @Bean
        LinkOperationProcessor linkOperationProcessor() {
            return Mockito.mock(LinkOperationProcessor.class);
        }

        @Bean
        NotificationManager notificationManager() {
            return Mockito.mock(NotificationManager.class);
        }

        @Bean
        LinkUpdateScheduledListener linkUpdateScheduledListener(
                ScrapperConfig scrapperConfig,
                DatabaseProperty databaseProperty,
                LinkUpdaterServiceFactory updaterFactory,
                LinkOperationProcessor linkOperationProcessor,
                NotificationManager notificationManager) {
            return new LinkUpdateScheduledListener(
                    scrapperConfig, databaseProperty, updaterFactory, linkOperationProcessor, notificationManager);
        }
    }
}

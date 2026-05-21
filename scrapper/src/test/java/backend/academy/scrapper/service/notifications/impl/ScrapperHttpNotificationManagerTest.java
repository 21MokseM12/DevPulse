package backend.academy.scrapper.service.notifications.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.ScrapperConfig.DeliveryMode;
import backend.academy.scrapper.db.repository.KafkaOutboxRepository;
import backend.academy.scrapper.mapper.LinkUpdateMapper;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.NotifyUpdateEntity;
import backend.academy.scrapper.service.resilience.ExternalApiResilienceExecutor;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import scrapper.bot.connectivity.model.LinkUpdate;

@ExtendWith(MockitoExtension.class)
class ScrapperHttpNotificationManagerTest {

    @Mock
    private BotClient botClient;

    @Mock
    private LinkUpdateMapper mapper;

    @Mock
    private KafkaOutboxRepository kafkaOutboxRepository;

    @Mock
    private ScrapperConfig scrapperConfig;

    @Mock
    private ScrapperConfig.OutboxCredentials outboxCredentials;

    @Mock
    private ScrapperConfig.DeliveryCredentials deliveryCredentials;

    @Mock
    private ExternalApiResilienceExecutor resilienceExecutor;

    @Test
    void notify_inHttpMode_sendsOnlyHttp() {
        ScrapperHttpNotificationManager manager = new ScrapperHttpNotificationManager(
                botClient, mapper, kafkaOutboxRepository, scrapperConfig, new ObjectMapper(), resilienceExecutor);

        LinkUpdateDTO update = new LinkUpdateDTO(10L, "title", "owner", OffsetDateTime.now(), "desc");
        NotifyUpdateEntity entity = new NotifyUpdateEntity(
                URI.create("https://github.com/acme/repo"), List.of(update), List.of("alice", "bob"));
        LinkUpdate payload = new LinkUpdate(
                10L,
                URI.create("https://github.com/acme/repo"),
                URI.create("https://github.com/acme/repo/pull/10"),
                "title",
                "owner",
                "desc",
                OffsetDateTime.now(),
                List.of("alice", "bob"));

        when(mapper.toLinkUpdate(update, entity)).thenReturn(payload);
        when(resilienceExecutor.execute(eq("bot-api"), any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Supplier<ResponseEntity<?>> supplier = invocation.getArgument(1);
            return supplier.get();
        });
        when(botClient.sendUpdates(eq(payload), any(HttpHeaders.class)))
                .thenReturn(ResponseEntity.ok().build());
        when(scrapperConfig.delivery()).thenReturn(deliveryCredentials);
        when(deliveryCredentials.mode()).thenReturn(DeliveryMode.HTTP);
        when(scrapperConfig.auth()).thenReturn(new ScrapperConfig.AuthCredentials("X-Internal-Secret", "secret"));

        manager.notify(List.of(entity));

        verify(kafkaOutboxRepository, times(0)).save(any(), any());
        verify(resilienceExecutor, times(1)).execute(eq("bot-api"), any());
        verify(botClient, times(1)).sendUpdates(eq(payload), org.mockito.ArgumentMatchers.argThat(headers -> "secret"
                .equals(headers.getFirst("X-Internal-Secret"))));
    }

    @Test
    void notify_inKafkaMode_writesOnlyOutbox() {
        ScrapperHttpNotificationManager manager = new ScrapperHttpNotificationManager(
                botClient, mapper, kafkaOutboxRepository, scrapperConfig, new ObjectMapper(), resilienceExecutor);

        LinkUpdateDTO update = new LinkUpdateDTO(10L, "title", "owner", OffsetDateTime.now(), "desc");
        NotifyUpdateEntity entity = new NotifyUpdateEntity(
                URI.create("https://github.com/acme/repo"), List.of(update), List.of("alice", "bob"));
        LinkUpdate payload = new LinkUpdate(
                10L,
                URI.create("https://github.com/acme/repo"),
                URI.create("https://github.com/acme/repo/pull/10"),
                "title",
                "owner",
                "desc",
                OffsetDateTime.now(),
                List.of("alice", "bob"));

        when(mapper.toLinkUpdate(update, entity)).thenReturn(payload);
        when(scrapperConfig.outbox()).thenReturn(outboxCredentials);
        when(outboxCredentials.topic()).thenReturn("link-updates");
        when(scrapperConfig.delivery()).thenReturn(deliveryCredentials);
        when(deliveryCredentials.mode()).thenReturn(DeliveryMode.KAFKA);

        manager.notify(List.of(entity));

        verify(kafkaOutboxRepository, times(1)).save("link-updates", payload);
        verify(resilienceExecutor, times(0)).execute(any(), any());
    }

    @Test
    void notify_inHttpMode_writesHumanReadableLogs() {
        ScrapperHttpNotificationManager manager = new ScrapperHttpNotificationManager(
                botClient, mapper, kafkaOutboxRepository, scrapperConfig, new ObjectMapper(), resilienceExecutor);

        LinkUpdateDTO update = new LinkUpdateDTO(10L, "title", "owner", OffsetDateTime.now(), "desc");
        URI link = URI.create("https://github.com/acme/repo");
        NotifyUpdateEntity entity = new NotifyUpdateEntity(link, List.of(update), List.of("alice"));
        LinkUpdate payload = new LinkUpdate(
                10L,
                link,
                URI.create("https://github.com/acme/repo/pull/10"),
                "title",
                "owner",
                "desc",
                OffsetDateTime.now(),
                List.of("alice"));

        when(mapper.toLinkUpdate(update, entity)).thenReturn(payload);
        when(resilienceExecutor.execute(eq("bot-api"), any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Supplier<ResponseEntity<?>> supplier = invocation.getArgument(1);
            return supplier.get();
        });
        when(botClient.sendUpdates(eq(payload), any(HttpHeaders.class)))
                .thenReturn(ResponseEntity.ok().build());
        when(scrapperConfig.delivery()).thenReturn(deliveryCredentials);
        when(deliveryCredentials.mode()).thenReturn(DeliveryMode.HTTP);
        when(scrapperConfig.auth()).thenReturn(new ScrapperConfig.AuthCredentials("X-Internal-Secret", "secret"));

        Logger logger = (Logger) LoggerFactory.getLogger(ScrapperHttpNotificationManager.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        logger.addAppender(appender);
        appender.start();
        try {
            manager.notify(List.of(entity));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }

        List<String> messages =
                appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
        assertThat(messages)
                .anyMatch(message -> message.contains("Начинается отправка уведомлений: ссылок=1"))
                .anyMatch(message -> message.contains("Начинается отправка уведомлений по ссылке: " + link))
                .anyMatch(message -> message.contains("Уведомления отправлены успешно по ссылке: " + link))
                .anyMatch(message -> message.contains("Отправка уведомлений завершена: доставлено=1"));
    }
}

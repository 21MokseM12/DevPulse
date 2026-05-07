package backend.academy.scrapper.service.notifications.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.db.repository.KafkaOutboxRepository;
import backend.academy.scrapper.mapper.LinkUpdateMapper;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.NotifyUpdateEntity;
import backend.academy.scrapper.service.resilience.ExternalApiResilienceExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    private ExternalApiResilienceExecutor resilienceExecutor;

    @Test
    void notify_writesEveryUpdateToKafkaOutbox() {
        ScrapperHttpNotificationManager manager = new ScrapperHttpNotificationManager(
                botClient, mapper, kafkaOutboxRepository, scrapperConfig, new ObjectMapper(), resilienceExecutor);

        LinkUpdateDTO update = new LinkUpdateDTO(10L, "title", "owner", OffsetDateTime.now(), "desc");
        NotifyUpdateEntity entity =
                new NotifyUpdateEntity(URI.create("https://github.com/acme/repo"), List.of(update), List.of(1L, 2L));
        LinkUpdate payload = new LinkUpdate(
                10L,
                URI.create("https://github.com/acme/repo"),
                "title",
                "owner",
                "desc",
                OffsetDateTime.now(),
                List.of(1L, 2L));

        when(mapper.toLinkUpdate(update, entity)).thenReturn(payload);
        when(resilienceExecutor.execute(eq("bot-api"), any()))
                .thenReturn(ResponseEntity.ok().build());
        when(scrapperConfig.outbox()).thenReturn(outboxCredentials);
        when(outboxCredentials.topic()).thenReturn("link-updates");

        manager.notify(List.of(entity));

        verify(kafkaOutboxRepository, times(1)).save("link-updates", payload);
        verify(resilienceExecutor, times(1)).execute(eq("bot-api"), any());
    }
}

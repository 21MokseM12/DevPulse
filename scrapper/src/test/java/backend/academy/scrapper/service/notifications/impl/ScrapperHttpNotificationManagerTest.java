package backend.academy.scrapper.service.notifications.impl;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.db.repository.KafkaOutboxRepository;
import backend.academy.scrapper.mapper.LinkUpdateMapper;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.NotifyUpdateEntity;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import scrapper.bot.connectivity.model.LinkUpdate;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScrapperHttpNotificationManagerTest {

    @Mock
    private BotClient botClient;
    @Mock
    private LinkUpdateMapper mapper;
    @Mock
    private KafkaOutboxRepository kafkaOutboxRepository;

    @Test
    void notify_writesEveryUpdateToKafkaOutbox() {
        ScrapperHttpNotificationManager manager =
            new ScrapperHttpNotificationManager(botClient, mapper, kafkaOutboxRepository);

        LinkUpdateDTO update = new LinkUpdateDTO(10L, "title", "owner", OffsetDateTime.now(), "desc");
        NotifyUpdateEntity entity = new NotifyUpdateEntity(
            URI.create("https://github.com/acme/repo"),
            List.of(update),
            List.of(1L, 2L)
        );
        LinkUpdate payload = new LinkUpdate(
            10L,
            URI.create("https://github.com/acme/repo"),
            "title",
            "owner",
            "desc",
            OffsetDateTime.now(),
            List.of(1L, 2L)
        );

        when(mapper.toLinkUpdate(update, entity)).thenReturn(payload);
        when(botClient.sendUpdates(payload)).thenReturn(ResponseEntity.ok().build());

        manager.notify(List.of(entity));

        verify(kafkaOutboxRepository, times(1)).save("link-updates", payload);
        verify(botClient, times(1)).sendUpdates(payload);
    }
}

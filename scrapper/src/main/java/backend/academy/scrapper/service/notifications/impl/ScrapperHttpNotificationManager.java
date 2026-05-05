package backend.academy.scrapper.service.notifications.impl;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.db.repository.KafkaOutboxRepository;
import backend.academy.scrapper.mapper.LinkUpdateMapper;
import backend.academy.scrapper.model.NotifyUpdateEntity;
import backend.academy.scrapper.service.notifications.NotificationManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.model.response.ApiErrorResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapperHttpNotificationManager implements NotificationManager {

    private final BotClient botClient;
    private final LinkUpdateMapper mapper;
    private final KafkaOutboxRepository kafkaOutboxRepository;
    private final ScrapperConfig scrapperConfig;
    private final ObjectMapper objectMapper;

    @Override
    public void notify(List<NotifyUpdateEntity> notifications) {
        for (NotifyUpdateEntity notification : notifications) {
            notification.updates().forEach(update -> {
                var linkUpdate = mapper.toLinkUpdate(update, notification);
                kafkaOutboxRepository.save(scrapperConfig.outbox().topic(), linkUpdate);
                ResponseEntity<?> response = botClient.sendUpdates(linkUpdate);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    ApiErrorResponse errorResponse =
                            objectMapper.convertValue(response.getBody(), ApiErrorResponse.class);
                    log.error("При отправлении обновления по ссылке произошла ошибка: {}", errorResponse);
                }
            });
        }
    }
}

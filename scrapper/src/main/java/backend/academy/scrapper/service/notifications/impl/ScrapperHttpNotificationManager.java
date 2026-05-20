package backend.academy.scrapper.service.notifications.impl;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.ScrapperConfig.DeliveryMode;
import backend.academy.scrapper.db.repository.KafkaOutboxRepository;
import backend.academy.scrapper.mapper.LinkUpdateMapper;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.NotifyUpdateEntity;
import backend.academy.scrapper.service.notifications.NotificationManager;
import backend.academy.scrapper.service.resilience.ExternalApiResilienceExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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
    private final ExternalApiResilienceExecutor resilienceExecutor;

    @Override
    public List<NotifyUpdateEntity> notify(List<NotifyUpdateEntity> notifications) {
        List<NotifyUpdateEntity> delivered = new ArrayList<>();
        for (NotifyUpdateEntity notification : notifications) {
            List<LinkUpdateDTO> deliveredUpdates = new ArrayList<>();
            notification.updates().forEach(update -> {
                var linkUpdate = mapper.toLinkUpdate(update, notification);
                if (scrapperConfig.delivery().mode() == DeliveryMode.KAFKA) {
                    kafkaOutboxRepository.save(scrapperConfig.outbox().topic(), linkUpdate);
                    deliveredUpdates.add(update);
                    return;
                }
                if (scrapperConfig.delivery().mode() == DeliveryMode.HTTP) {
                    try {
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(
                                scrapperConfig.auth().header(),
                                scrapperConfig.auth().sharedSecret());
                        ResponseEntity<?> response =
                                resilienceExecutor.execute("bot-api", () -> botClient.sendUpdates(linkUpdate, headers));
                        if (response != null && response.getStatusCode().is2xxSuccessful()) {
                            deliveredUpdates.add(update);
                        } else if (response != null) {
                            ApiErrorResponse errorResponse =
                                    objectMapper.convertValue(response.getBody(), ApiErrorResponse.class);
                            log.error("При отправлении обновления по ссылке произошла ошибка: {}", errorResponse);
                        } else {
                            log.error("При отправлении обновления по ссылке произошла ошибка: пустой ответ от bot-api");
                        }
                    } catch (Exception ex) {
                        log.error("Ошибка resilient-отправки обновления в bot: {}", ex.getMessage());
                    }
                }
            });
            if (!deliveredUpdates.isEmpty()) {
                delivered.add(new NotifyUpdateEntity(
                        notification.link(), deliveredUpdates, List.copyOf(notification.clientLogins())));
            }
        }
        return delivered;
    }
}

package backend.academy.scrapper.service.notifications.impl;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.model.NotifyUpdateEntity;
import backend.academy.scrapper.service.notifications.NotificationManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.model.LinkUpdate;
import scrapper.bot.connectivity.model.response.ApiErrorResponse;

@Service
@Slf4j
public class ScrapperHttpNotificationManager implements NotificationManager {

    private final BotClient botClient;

    @Autowired
    public ScrapperHttpNotificationManager(BotClient botClient) {
        this.botClient = botClient;
    }

    @Override
    public void notify(List<NotifyUpdateEntity> notifications) {
        for (NotifyUpdateEntity notification : notifications) {
            notification.updates().forEach(update -> {
                ResponseEntity<?> response = botClient.sendUpdates(new LinkUpdate(
                        update.id(),
                        notification.link(),
                        update.title(),
                        update.updateOwner(),
                        update.descriptionPreview(),
                        update.creationDate(),
                        notification.chatIds()));
                if (!response.getStatusCode().is2xxSuccessful()) {
                    ObjectMapper mapper = new ObjectMapper();
                    ApiErrorResponse errorResponse = mapper.convertValue(response.getBody(), ApiErrorResponse.class);
                    log.error("Error occur via sending request by link\nError: {}", errorResponse);
                }
            });
        }
    }
}

package backend.academy.scrapper.service.notifications.impl;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.service.notifications.NotificationManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.model.LinkUpdate;
import scrapper.bot.connectivity.model.response.ApiErrorResponse;

@Service
@Slf4j
public class ScrapperHttpNotificationManager implements NotificationManager {

    private long updateId = 1;

    @Autowired
    private BotClient botClient;

    public void notify(Map<URI, List<Long>> notificationsMap) {
        for (Map.Entry<URI, List<Long>> entry : notificationsMap.entrySet()) {
            ResponseEntity<?> response = botClient.sendUpdates(new LinkUpdate(
                    updateId++,
                    entry.getKey(),
                    "Url ".concat(entry.getKey().toString()).concat("was updated"),
                    entry.getValue()));
            if (!response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                ApiErrorResponse errorResponse = mapper.convertValue(response.getBody(), ApiErrorResponse.class);
                log.error("Error occur via sending request by link\nError: {}", errorResponse);
            }
        }
    }
}

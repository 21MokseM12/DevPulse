package backend.academy.bot.controller;

import backend.academy.bot.service.notifications.BotNotificationManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.LinkUpdate;

@RestController
@RequestMapping("/updates")
@RequiredArgsConstructor
public class ScrapperController {

    private final BotNotificationManager notificationManager;

    @PostMapping
    public ResponseEntity<Void> notifyLinkUpdate(@RequestBody LinkUpdate update) throws BadRequestException {
        if (update.clientsIds().isEmpty() || update.url() == null) {
            throw new BadRequestException("Некорректные параметры запроса");
        }
        notificationManager.notify(update);
        return ResponseEntity.ok().build();
    }
}

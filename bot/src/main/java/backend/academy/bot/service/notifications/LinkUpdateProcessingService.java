package backend.academy.bot.service.notifications;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.LinkUpdate;

@Service
@RequiredArgsConstructor
public class LinkUpdateProcessingService {

    private final BotNotificationManager notificationManager;

    public void process(LinkUpdate update) throws BadRequestException {
        validate(update);
        notificationManager.notify(update);
    }

    private void validate(LinkUpdate update) throws BadRequestException {
        if (update.clientsIds() == null
                || update.clientsIds().isEmpty()
                || update.url() == null
                || update.title() == null
                || update.updateOwner() == null
                || update.description() == null
                || update.creationDate() == null) {
            throw new BadRequestException("Некорректные параметры запроса");
        }
    }
}

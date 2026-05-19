package backend.academy.bot.mapper;

import backend.academy.bot.db.model.Notification;
import org.springframework.stereotype.Component;
import scrapper.bot.connectivity.model.LinkUpdate;

@Component
public class LinkUpdateNotificationMapper {

    public Notification map(LinkUpdate update) {
        return new Notification(
                update.id(),
                update.url().toString(),
                update.eventUrl() == null ? null : update.eventUrl().toString(),
                update.title(),
                update.updateOwner(),
                update.description(),
                update.creationDate(),
                update.clientLogins());
    }
}

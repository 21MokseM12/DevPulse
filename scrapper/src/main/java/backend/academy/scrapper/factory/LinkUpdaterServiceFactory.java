package backend.academy.scrapper.factory;

import backend.academy.scrapper.service.updaters.LinkUpdater;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scrapper.bot.connectivity.enums.LinkUpdaterType;

@Component
public class LinkUpdaterServiceFactory {

    private final Map<LinkUpdaterType, LinkUpdater> updaters;

    @Autowired
    public LinkUpdaterServiceFactory(List<LinkUpdater> updatersList) {
        this.updaters = new HashMap<>();
        for (LinkUpdater updater : updatersList) {
            updaters.put(updater.getType(), updater);
        }
    }

    public LinkUpdater get(URI link) {
        return updaters.get(LinkUpdaterType.fromLink(link.toString()));
    }
}

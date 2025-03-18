package backend.academy.scrapper.service.updaters;

import backend.academy.scrapper.model.LinkUpdateDTO;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import scrapper.bot.connectivity.enums.LinkUpdaterType;

public interface LinkUpdater {

    List<LinkUpdateDTO> getUpdates(URI link);

    LinkUpdaterType getType();
}

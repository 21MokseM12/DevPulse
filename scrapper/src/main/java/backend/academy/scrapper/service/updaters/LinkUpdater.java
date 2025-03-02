package backend.academy.scrapper.service.updaters;

import backend.academy.scrapper.enums.LinkUpdaterType;
import backend.academy.scrapper.model.LinkUpdateDTO;
import java.net.URI;
import java.util.List;
import java.util.Optional;

public interface LinkUpdater {

    Optional<List<LinkUpdateDTO>> getUpdates(URI link);

    LinkUpdaterType getType();
}

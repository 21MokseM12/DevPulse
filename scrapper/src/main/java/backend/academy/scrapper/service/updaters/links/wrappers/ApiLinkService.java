package backend.academy.scrapper.service.updaters.links.wrappers;

import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import java.net.URI;
import java.util.List;

public interface ApiLinkService {
    void saveProcessedIds(URI link, List<ProcessedIdDTO> nowProcessedIds);
}

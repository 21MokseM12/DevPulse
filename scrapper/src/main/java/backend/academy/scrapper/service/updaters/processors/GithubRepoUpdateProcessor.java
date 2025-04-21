package backend.academy.scrapper.service.updaters.processors;

import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.github.GithubResponse;
import java.net.URI;
import java.util.List;

public interface GithubRepoUpdateProcessor {

    List<LinkUpdateDTO> processUpdates(URI link, List<GithubResponse> updates);
}

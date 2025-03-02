package backend.academy.scrapper.service.updaters.impl;

import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.enums.LinkUpdaterType;
import backend.academy.scrapper.model.GithubResponse;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.service.parsers.GithubLinkParser;
import backend.academy.scrapper.service.updaters.LinkUpdater;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GithubUpdaterService implements LinkUpdater {

    private static long updateId = 1;

    @Autowired
    private GithubLinkParser linkParser;

    @Autowired
    private GithubClient githubClient;

    @Override
    public Optional<List<LinkUpdateDTO>> getUpdates(URI link) {
        ResponseEntity<List<GithubResponse>> events = githubClient.getEvents(
                linkParser.parseUsername(link.toString()), linkParser.parseRepo(link.toString()));
        if (events.getStatusCode().is2xxSuccessful()
                && !Objects.requireNonNull(events.getBody()).isEmpty()) {
            return Optional.of(Objects.requireNonNull(events.getBody()).stream()
                    .map(response -> new LinkUpdateDTO(
                            updateId++,
                            link,
                            "Обновление в ".concat(response.create().toString())))
                    .toList());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public LinkUpdaterType getType() {
        return LinkUpdaterType.GITHUB;
    }
}

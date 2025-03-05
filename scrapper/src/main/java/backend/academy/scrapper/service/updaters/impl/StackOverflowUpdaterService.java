package backend.academy.scrapper.service.updaters.impl;

import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.StackOverflowResponse;
import backend.academy.scrapper.service.parsers.StackOverflowLinkParser;
import backend.academy.scrapper.service.updaters.LinkUpdater;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.enums.LinkUpdaterType;

@Service
public class StackOverflowUpdaterService implements LinkUpdater {

    private static long updateId = 1;

    @Autowired
    private StackOverflowClient stackOverflowClient;

    @Autowired
    private StackOverflowLinkParser stackOverflowLinkParser;

    @Override
    public Optional<List<LinkUpdateDTO>> getUpdates(URI link) {
        ResponseEntity<List<StackOverflowResponse>> events =
                stackOverflowClient.getEvents(stackOverflowLinkParser.parseQuestionId(link.toString()));
        if (events.getStatusCode().is2xxSuccessful()
                && !Objects.requireNonNull(events.getBody()).isEmpty()) {
            return Optional.of(Objects.requireNonNull(events.getBody()).stream()
                    .filter(Objects::nonNull)
                    .map(update -> new LinkUpdateDTO(
                            updateId++,
                            update.postLink(),
                            "Last activity date: ".concat(update.lastActivity().toString())))
                    .toList());
        }
        return Optional.empty();
    }

    @Override
    public LinkUpdaterType getType() {
        return LinkUpdaterType.STACK_OVERFLOW;
    }
}

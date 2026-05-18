package backend.academy.scrapper.service;

import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;

public interface LinkOperationProcessor {
    List<LinkResponse> findAllByChatId(Long chatId);

    Optional<LinkResponse> subscribe(Long chatId, AddLinkRequest link);

    Optional<LinkResponse> unsubscribe(Long chatId, RemoveLinkRequest uri);

    List<ProcessedIdDTO> findAllProcessedIds(URI link);

    void saveProcessedIds(URI link, List<ProcessedIdDTO> nowProcessedIds);

    Set<URI> findAllLinksByForceCheckDelay(Duration duration, int pageNum);

    List<Long> findSubscribedChats(URI link);

    List<Long> findSubscribedChats(URI link, LinkUpdateDTO update);

    List<String> findClientLogins(List<Long> chatIds);

    void markPollingSuccess(URI link, OffsetDateTime checkedAt, Duration forceCheckDelay);

    void markPollingFailure(URI link, OffsetDateTime checkedAt, Duration forceCheckDelay, String error);
}

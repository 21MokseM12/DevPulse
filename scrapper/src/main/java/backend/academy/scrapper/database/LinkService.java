package backend.academy.scrapper.database;

import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;

public interface LinkService {
    List<LinkResponse> findAllByChatId(Long chatId);

    Optional<LinkResponse> subscribe(Long chatId, AddLinkRequest link);

    Optional<LinkResponse> unsubscribe(Long chatId, RemoveLinkRequest uri);

    List<ProcessedIdDTO> findAllProcessedIds(URI link);

    void saveProcessedIds(URI link, List<ProcessedIdDTO> nowProcessedIds);

    Set<URI> findAllLinksByForceCheckDelay(Duration duration, int pageNum);

    List<Long> findSubscribedChats(URI link);
}

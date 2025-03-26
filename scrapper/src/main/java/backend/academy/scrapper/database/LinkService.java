package backend.academy.scrapper.database;

import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import jakarta.validation.constraints.NotEmpty;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;

public interface LinkService {
    List<LinkResponse> findAllByChatId(Long chatId);

    Optional<LinkResponse> subscribe(Long chatId, AddLinkRequest link);

    Optional<LinkResponse> unsubscribe(Long chatId, RemoveLinkRequest uri);

    List<ProcessedIdDTO> findAllProcessedIds(URI link);

    void saveProcessedIds(URI link, List<ProcessedIdDTO> nowProcessedIds);

    Stream<URI> findAllLinksByForceCheckDelay(@NotEmpty Duration duration);

    List<Long> findSubscribedChats(URI link);
}

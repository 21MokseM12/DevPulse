package backend.academy.scrapper.database;

import java.util.List;
import java.util.Optional;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;

public interface LinkService {
    List<LinkResponse> findAllByChatId(Long chatId);

    Optional<LinkResponse> subscribe(Long chatId, AddLinkRequest link);

    Optional<LinkResponse> unsubscribe(Long chatId, RemoveLinkRequest uri);
}

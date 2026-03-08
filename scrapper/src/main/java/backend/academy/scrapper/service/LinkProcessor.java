package backend.academy.scrapper.service;

import java.util.List;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;

public interface LinkProcessor {

    List<LinkResponse> findAll(Long chatId);

    LinkResponse subscribeLink(Long chatId, AddLinkRequest request);

    LinkResponse unsubscribeLink(Long chatId, RemoveLinkRequest request);
}

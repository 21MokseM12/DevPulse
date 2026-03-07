package backend.academy.scrapper.service;

import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;
import scrapper.bot.connectivity.model.response.ListLinkResponse;

public interface LinkProcessor {

    ListLinkResponse findAll(Long chatId);

    LinkResponse subscribeLink(Long chatId, AddLinkRequest request);

    LinkResponse unsubscribeLink(Long chatId, RemoveLinkRequest request);
}

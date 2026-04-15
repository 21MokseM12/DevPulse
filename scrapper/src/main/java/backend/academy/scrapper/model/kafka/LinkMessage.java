package backend.academy.scrapper.model.kafka;

import backend.academy.scrapper.enums.actions.LinkActions;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;

public record LinkMessage(
    LinkActions action,
    String login,
    String password,
    AddLinkRequest add,
    RemoveLinkRequest remove
) { }

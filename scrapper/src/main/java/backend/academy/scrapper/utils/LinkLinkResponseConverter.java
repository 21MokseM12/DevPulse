package backend.academy.scrapper.utils;

import backend.academy.scrapper.model.Link;
import lombok.experimental.UtilityClass;
import scrapper.bot.connectivity.model.response.LinkResponse;

@UtilityClass
public class LinkLinkResponseConverter {

    public static LinkResponse convert(final Link link) {
        return new LinkResponse(link.id(), link.url(), link.tags(), link.filters());
    }
}

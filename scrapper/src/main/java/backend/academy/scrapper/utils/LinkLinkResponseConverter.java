package backend.academy.scrapper.utils;

import lombok.experimental.UtilityClass;
import backend.academy.scrapper.model.Link;
import scrapper.bot.connectivity.model.response.LinkResponse;

@UtilityClass
public class LinkLinkResponseConverter {

    public static LinkResponse convert(final Link link) {
        return new LinkResponse(
            link.id(),
            link.url(),
            link.tags(),
            link.filters()
        );
    }
}

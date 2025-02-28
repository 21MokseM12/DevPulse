package backend.academy.scrapper.utils;

import lombok.experimental.UtilityClass;
import scrapper.bot.connectivity.model.Link;
import scrapper.bot.connectivity.model.LinkRequest;

@UtilityClass
public class LinkDTOConverter {

    public static Link toEntity(LinkRequest linkRequest, Integer linkId) {
        return new Link(
            linkId,
            linkRequest.uri(),
            linkRequest.tags(),
            linkRequest.filters()
        );
    }
}

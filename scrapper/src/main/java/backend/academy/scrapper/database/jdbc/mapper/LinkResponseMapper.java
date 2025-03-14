package backend.academy.scrapper.database.jdbc.mapper;

import backend.academy.scrapper.database.model.Link;
import scrapper.bot.connectivity.model.response.LinkResponse;

public class LinkResponseMapper {

    public static LinkResponse map(Link link) {
        return new LinkResponse(link.id(), link.url(), link.tags(), link.filters());
    }
}

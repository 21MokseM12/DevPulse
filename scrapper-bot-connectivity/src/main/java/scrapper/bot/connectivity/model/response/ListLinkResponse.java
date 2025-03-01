package scrapper.bot.connectivity.model.response;

import java.util.List;

public record ListLinkResponse(
    List<LinkResponse> links,
    int size
) {
}

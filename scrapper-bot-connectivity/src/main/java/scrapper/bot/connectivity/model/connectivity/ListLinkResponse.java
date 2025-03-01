package scrapper.bot.connectivity.model.connectivity;

import java.util.List;

public record ListLinkResponse(
    List<LinkResponse> links,
    int size
) {
}

package scrapper.bot.connectivity.model.request;

import java.net.URI;
import java.util.List;

public record AddLinkRequest(
    URI link,
    List<String> tags,
    List<String> filters
) {
}

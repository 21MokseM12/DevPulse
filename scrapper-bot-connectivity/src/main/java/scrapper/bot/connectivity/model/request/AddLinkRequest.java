package scrapper.bot.connectivity.model.request;

import java.util.List;

public record AddLinkRequest(
    String link,
    List<String> tags,
    List<String> filters
) {
}

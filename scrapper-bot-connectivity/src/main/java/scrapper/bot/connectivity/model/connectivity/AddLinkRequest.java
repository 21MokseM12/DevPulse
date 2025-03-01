package scrapper.bot.connectivity.model.connectivity;

import java.util.List;

public record AddLinkRequest(
    String link,
    List<String> tags,
    List<String> filters
) {
}

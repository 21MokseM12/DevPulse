package scrapper.bot.connectivity.model.response;

import java.util.List;

public record LinkResponse(
    Long id,
    String url,
    List<String> tags,
    List<String> filters
) {
}

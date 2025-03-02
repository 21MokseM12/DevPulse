package scrapper.bot.connectivity.model.response;

import java.net.URI;
import java.util.List;

public record LinkResponse(Long id, URI url, List<String> tags, List<String> filters) {}

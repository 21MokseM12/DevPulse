package scrapper.bot.connectivity.model.response;

import java.net.URI;
import java.util.Set;

public record LinkResponse(Long id, URI url, Set<String> tags, Set<String> filters) {}

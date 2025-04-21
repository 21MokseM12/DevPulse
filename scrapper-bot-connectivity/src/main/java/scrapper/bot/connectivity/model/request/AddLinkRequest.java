package scrapper.bot.connectivity.model.request;

import java.net.URI;
import java.util.Set;

public record AddLinkRequest(URI link, Set<String> tags, Set<String> filters) {}

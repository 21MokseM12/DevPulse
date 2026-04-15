package backend.academy.bot.utils;

import java.net.URI;
import java.util.Set;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LinkValidator {
    private static final Set<String> SUPPORTED_DOMAINS = Set.of("github.com", "stackoverflow.com");

    public boolean isValid(String link) {
        if (link == null) {
            return false;
        } else if (link.isBlank()) {
            return false;
        }
        URI parsedLink;
        try {
            parsedLink = URI.create(link.trim());
        } catch (IllegalArgumentException e) {
            return false;
        }

        if (!"https".equalsIgnoreCase(parsedLink.getScheme())) {
            return false;
        }

        String host = parsedLink.getHost();
        if (host == null || host.isBlank()) {
            return false;
        }
        host = host.toLowerCase();
        if (host.startsWith("www.")) {
            host = host.substring(4);
        }

        String path = parsedLink.getPath();
        if (path == null || path.isBlank() || "/".equals(path)) {
            return false;
        }

        return SUPPORTED_DOMAINS.contains(host);
    }
}

package backend.academy.bot.service.validators;

import org.springframework.stereotype.Component;

@Component
public class LinkValidator {

    private static final String LINK_PATTERN =
            "^(https?:\\/\\/)?(([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,})(:\\d{1,5})?(\\/\\S*)?$";

    public boolean validLink(String link) {
        if (link == null) {
            return false;
        } else if (link.isBlank()) {
            return false;
        } else {
            return link.matches(LINK_PATTERN);
        }
    }
}

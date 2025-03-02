package backend.academy.bot.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LinkValidator {

    private static final String LINK_PATTERN = "^(https?://)?(([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,})(:\\d{1,5})?(/\\S*)?$";

    public boolean isValid(String link) {
        if (link == null) {
            return false;
        } else if (link.isBlank()) {
            return false;
        } else {
            return link.matches(LINK_PATTERN);
        }
    }
}

package backend.academy.bot.utils;

import lombok.experimental.UtilityClass;
import scrapper.bot.connectivity.enums.LinkUpdaterType;

@UtilityClass
public class LinkValidator {

    public boolean isValid(String link) {
        if (link == null) {
            return false;
        } else if (link.isBlank()) {
            return false;
        }
        String[] splitLink = link.replace("//", "/").split("/");
        if (splitLink.length < 4) {
            return false;
        }
        if (!splitLink[0].equals("https:")) {
            return false;
        }
        for (LinkUpdaterType type : LinkUpdaterType.values()) {
            if (splitLink[1].equals(type.domain())) {
                return true;
            }
        }
        return false;
    }
}

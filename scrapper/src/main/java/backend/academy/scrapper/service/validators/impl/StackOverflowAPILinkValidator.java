package backend.academy.scrapper.service.validators.impl;

import backend.academy.scrapper.service.validators.APILinkValidator;
import org.springframework.stereotype.Component;

@Component
// https://stackoverflow.com/questions/79479316/how-to-change-microsoft-azure-signalr-emulator-access-keys
public class StackOverflowAPILinkValidator implements APILinkValidator {

    @Override
    public boolean isValidLink(String link) {
        if (link == null) {
            return false;
        }
        if (link.isBlank()) {
            return false;
        }
        link = link.replace("//", "/");
        String[] splitLink = link.split("/");
        if (splitLink.length != 5) {
            return false;
        }
        return splitLink[0].equals("https:")
                && splitLink[1].equals("stackoverflow.com")
                && splitLink[2].equals("questions")
                && splitLink[3].matches("\\d+");
    }
}

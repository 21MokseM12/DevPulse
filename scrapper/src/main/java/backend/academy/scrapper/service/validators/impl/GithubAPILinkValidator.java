package backend.academy.scrapper.service.validators.impl;

import backend.academy.scrapper.service.validators.APILinkValidator;
import org.springframework.stereotype.Component;

@Component
// https://github.com/21MokseM12/Log-analyzer-Tbank-project
public class GithubAPILinkValidator implements APILinkValidator {

    @Override
    public boolean isValidLink(String link) {
        link = link.replace("//", "/");
        String[] splitLink = link.split("/");
        if (splitLink.length != 4) {
            return false;
        }
        if (!splitLink[0].equals("https:")) {
            return false;
        }
        return splitLink[1].equals("github.com");
    }
}

package backend.academy.scrapper.service.parsers;

import org.springframework.stereotype.Component;

@Component
// https://github.com/21MokseM12/Log-analyzer-Tbank-project
public class GithubLinkParser {

    public String parseRepo(String link) {
        return link
            .replace("//", "/")
            .split("/")[3];
    }

    public String parseUsername(String link) {
        return link
            .replace("//", "/")
            .split("/")[2];
    }
}

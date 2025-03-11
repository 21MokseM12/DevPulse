package backend.academy.scrapper.service.parsers;

import org.springframework.stereotype.Component;

@Component
// https://stackoverflow.com/questions/79479316/how-to-change-microsoft-azure-signalr-emulator-access-keys
public class StackOverflowLinkParser {

    public Long parseQuestionId(String link) {
        return Long.parseLong(link.replace("//", "/").split("/")[3]);
    }
}

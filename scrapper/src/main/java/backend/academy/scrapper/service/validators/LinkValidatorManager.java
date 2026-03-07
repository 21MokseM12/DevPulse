package backend.academy.scrapper.service.validators;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkValidatorManager {

    private final List<APILinkValidator> validators;

    public boolean isValidLink(String link) {
        for (APILinkValidator validator : validators) {
            if (validator.isValidLink(link)) {
                return true;
            }
        }
        return false;
    }
}

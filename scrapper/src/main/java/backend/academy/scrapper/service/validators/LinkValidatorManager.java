package backend.academy.scrapper.service.validators;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LinkValidatorManager {

    @Autowired
    private List<APILinkValidator> validators;

    public boolean isValidLink(String link) {
        for (APILinkValidator validator : validators) {
            if (validator.isValidLink(link)) {
                return true;
            }
        }
        return false;
    }
}

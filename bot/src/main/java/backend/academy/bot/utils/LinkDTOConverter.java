package backend.academy.bot.utils;

import backend.academy.bot.model.LinkDTO;
import org.springframework.stereotype.Component;
import scrapper.bot.connectivity.model.LinkRequest;

@Component
public class LinkDTOConverter {

    public LinkRequest toLinkRequest(final LinkDTO linkDTO) {
        return new LinkRequest(
            linkDTO.uri(),
            linkDTO.tags(),
            linkDTO.filters()
        );
    }
}

package backend.academy.scrapper.service.updaters.links.wrappers.impl;

import backend.academy.scrapper.database.LinkService;
import backend.academy.scrapper.enums.ProcessedIdType;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import backend.academy.scrapper.service.updaters.links.wrappers.ApiLinkService;
import java.net.URI;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StackOverflowLinkService implements ApiLinkService {

    private final LinkService linkService;

    @Autowired
    public StackOverflowLinkService(LinkService linkService) {
        this.linkService = linkService;
    }

    public List<Long> getProcessedCommentsIds(URI link) {
        return linkService.findAllProcessedIds(link).stream()
                .filter(id -> id.type() == ProcessedIdType.STACKOVERFLOW_COMMENT)
                .map(ProcessedIdDTO::id)
                .toList();
    }

    public List<Long> getProcessedAnswersIds(URI link) {
        return linkService.findAllProcessedIds(link).stream()
                .filter(id -> id.type() == ProcessedIdType.STACKOVERFLOW_ANSWER)
                .map(ProcessedIdDTO::id)
                .toList();
    }

    public void saveProcessedIds(URI link, List<ProcessedIdDTO> nowProcessedIds) {
        linkService.saveProcessedIds(link, nowProcessedIds);
    }
}

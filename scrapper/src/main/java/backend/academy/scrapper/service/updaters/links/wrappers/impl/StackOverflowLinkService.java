package backend.academy.scrapper.service.updaters.links.wrappers.impl;

import backend.academy.scrapper.enums.ProcessedIdType;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import backend.academy.scrapper.service.LinkOperationProcessor;
import backend.academy.scrapper.service.updaters.links.wrappers.ApiLinkService;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StackOverflowLinkService implements ApiLinkService {

    private final LinkOperationProcessor linkOperationProcessor;

    public List<Long> getProcessedCommentsIds(URI link) {
        return linkOperationProcessor.findAllProcessedIds(link).stream()
                .filter(id -> id.type() == ProcessedIdType.STACKOVERFLOW_COMMENT)
                .map(ProcessedIdDTO::id)
                .toList();
    }

    public List<Long> getProcessedAnswersIds(URI link) {
        return linkOperationProcessor.findAllProcessedIds(link).stream()
                .filter(id -> id.type() == ProcessedIdType.STACKOVERFLOW_ANSWER)
                .map(ProcessedIdDTO::id)
                .toList();
    }

    public void saveProcessedIds(URI link, List<ProcessedIdDTO> nowProcessedIds) {
        linkOperationProcessor.saveProcessedIds(link, nowProcessedIds);
    }
}

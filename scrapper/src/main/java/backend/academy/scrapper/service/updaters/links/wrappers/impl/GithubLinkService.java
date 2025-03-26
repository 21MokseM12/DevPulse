package backend.academy.scrapper.service.updaters.links.wrappers.impl;

import backend.academy.scrapper.database.LinkService;
import backend.academy.scrapper.enums.ProcessedIdType;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import backend.academy.scrapper.service.updaters.links.wrappers.ApiLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.util.List;

@Service
public class GithubLinkService implements ApiLinkService {

    private final LinkService linkService;

    @Autowired
    public GithubLinkService(LinkService linkService) {
        this.linkService = linkService;
    }

    public List<Long> getProcessedPullRequestIds(URI link) {
        return linkService.findAllProcessedIds(link).stream()
            .filter(id -> id.type() == ProcessedIdType.GITHUB_PULL_REQUEST)
            .map(ProcessedIdDTO::id)
            .toList();
    }

    public List<Long> getProcessedIssueIds(URI link) {
        return linkService.findAllProcessedIds(link).stream()
            .filter(id -> id.type() == ProcessedIdType.GITHUB_ISSUE)
            .map(ProcessedIdDTO::id)
            .toList();
    }

    @Override
    public void saveProcessedIds(URI link, List<ProcessedIdDTO> nowProcessedIds) {
        linkService.saveProcessedIds(link, nowProcessedIds);
    }
}

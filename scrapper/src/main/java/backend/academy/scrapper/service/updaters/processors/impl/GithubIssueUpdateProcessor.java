package backend.academy.scrapper.service.updaters.processors.impl;

import backend.academy.scrapper.enums.GithubActionType;
import backend.academy.scrapper.enums.ProcessedIdType;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.github.GithubResponse;
import backend.academy.scrapper.model.github.mappers.GithubResponseMapper;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import backend.academy.scrapper.service.updaters.links.wrappers.impl.GithubLinkService;
import backend.academy.scrapper.service.updaters.processors.GithubRepoUpdateProcessor;
import java.net.URI;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GithubIssueUpdateProcessor implements GithubRepoUpdateProcessor {

    private final GithubLinkService githubLinkService;

    @Autowired
    public GithubIssueUpdateProcessor(GithubLinkService githubLinkService) {
        this.githubLinkService = githubLinkService;
    }

    @Override
    public List<LinkUpdateDTO> processUpdates(URI link, List<GithubResponse> updates) {
        List<Long> processedIds = githubLinkService.getProcessedIssueIds(link);
        List<LinkUpdateDTO> processedUpdates = updates.stream()
                .filter(event -> event.type().equals(GithubActionType.ISSUE_EVENT.type()))
                .filter(event -> event.payload().action().equals("opened"))
                .filter(event -> !processedIds.contains(event.id()))
                .map(GithubResponseMapper::mapToIssue)
                .toList();
        githubLinkService.saveProcessedIds(
                link,
                processedUpdates.stream()
                        .map(update -> new ProcessedIdDTO(update.id(), ProcessedIdType.GITHUB_ISSUE))
                        .toList());
        return processedUpdates;
    }
}

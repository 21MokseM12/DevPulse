package backend.academy.scrapper.service.updaters.processors.impl;

import backend.academy.scrapper.enums.GithubActionType;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.github.GithubResponse;
import backend.academy.scrapper.model.github.mappers.GithubResponseMapper;
import backend.academy.scrapper.service.updaters.links.wrappers.impl.GithubLinkService;
import backend.academy.scrapper.service.updaters.processors.GithubRepoUpdateProcessor;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GithubIssueUpdateProcessor implements GithubRepoUpdateProcessor {

    private final GithubLinkService githubLinkService;

    @Override
    public List<LinkUpdateDTO> processUpdates(URI link, List<GithubResponse> updates) {
        List<Long> processedIds = githubLinkService.getProcessedIssueIds(link);
        List<LinkUpdateDTO> processedUpdates = updates.stream()
                .filter(event -> event.type().equals(GithubActionType.ISSUE_EVENT.type()))
                .filter(event -> event.payload().action().equals("opened"))
                .filter(event -> !processedIds.contains(event.id()))
                .map(event -> GithubResponseMapper.mapToIssue(event, link))
                .toList();
        return processedUpdates;
    }
}

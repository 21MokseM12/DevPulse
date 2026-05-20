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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GithubCommitUpdateProcessor implements GithubRepoUpdateProcessor {

    private final GithubLinkService githubLinkService;

    @Override
    public List<LinkUpdateDTO> processUpdates(URI link, List<GithubResponse> updates) {
        List<Long> processedIds = githubLinkService.getProcessedCommitIds(link);
        List<LinkUpdateDTO> processedUpdates = updates.stream()
                .filter(event -> event.type().equals(GithubActionType.PUSH_EVENT.type()))
                .filter(event -> !processedIds.contains(event.id()))
                .peek(event -> {
                    if (event.payload().commits() == null
                            || event.payload().commits().isEmpty()) {
                        log.info(
                                "PushEvent {} для ссылки {} не содержит commits в payload. "
                                        + "Используется fallback-описание по ref/head.",
                                event.id(),
                                link);
                    }
                })
                .map(event -> GithubResponseMapper.mapToCommit(event, link))
                .toList();
        return processedUpdates;
    }
}

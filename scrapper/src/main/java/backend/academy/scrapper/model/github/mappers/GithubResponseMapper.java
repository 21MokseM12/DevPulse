package backend.academy.scrapper.model.github.mappers;

import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.UpdateType;
import backend.academy.scrapper.model.github.GithubLabel;
import backend.academy.scrapper.model.github.GithubResponse;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GithubResponseMapper {

    private static final int bodyLength = 200;

    public static LinkUpdateDTO mapToPullRequest(GithubResponse response) {
        String bodyPreview = response.payload().pullRequest().body().length() > bodyLength
                ? response.payload()
                        .pullRequest()
                        .body()
                        .substring(bodyLength + 1)
                        .concat("...")
                : response.payload().pullRequest().body();
        return new LinkUpdateDTO(
                response.id(),
                response.payload().pullRequest().title(),
                response.actor().login(),
                response.creationDate(),
                bodyPreview,
                UpdateType.GITHUB_PULL_REQUEST,
                extractLabels(response.payload().pullRequest().labels()));
    }

    public static LinkUpdateDTO mapToIssue(GithubResponse response) {
        String bodyPreview = response.payload().issue().body().length() > bodyLength
                ? response.payload().issue().body().substring(bodyLength + 1).concat("...")
                : response.payload().issue().body();
        return new LinkUpdateDTO(
                response.id(),
                response.payload().issue().title(),
                response.actor().login(),
                response.creationDate(),
                bodyPreview,
                UpdateType.GITHUB_ISSUE,
                extractLabels(response.payload().issue().labels()));
    }

    private static Set<String> extractLabels(List<GithubLabel> labels) {
        if (labels == null || labels.isEmpty()) {
            return Set.of();
        }
        return labels.stream()
                .map(GithubLabel::name)
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .collect(Collectors.toSet());
    }
}

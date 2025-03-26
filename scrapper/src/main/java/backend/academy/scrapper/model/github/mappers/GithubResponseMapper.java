package backend.academy.scrapper.model.github.mappers;

import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.github.GithubResponse;

public class GithubResponseMapper {

    private static final int bodyLength = 200;

    public static LinkUpdateDTO mapToPullRequest(GithubResponse response) {
        String bodyPreview = response.payload().pullRequest().body().length() > bodyLength ?
            response.payload().pullRequest().body().substring(bodyLength + 1).concat("...") :
            response.payload().pullRequest().body();
        return new LinkUpdateDTO(
            response.id(),
            response.payload().pullRequest().title(),
            response.actor().login(),
            response.creationDate(),
            bodyPreview
        );
    }

    public static LinkUpdateDTO mapToIssue(GithubResponse response) {
        String bodyPreview = response.payload().issue().body().length() > bodyLength ?
            response.payload().issue().body().substring(bodyLength + 1).concat("...") :
            response.payload().issue().body();
        return new LinkUpdateDTO(
            response.id(),
            response.payload().issue().title(),
            response.actor().login(),
            response.creationDate(),
            bodyPreview
        );
    }
}

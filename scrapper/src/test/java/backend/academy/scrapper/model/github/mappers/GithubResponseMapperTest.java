package backend.academy.scrapper.model.github.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.github.GithubActor;
import backend.academy.scrapper.model.github.GithubIssue;
import backend.academy.scrapper.model.github.GithubPayload;
import backend.academy.scrapper.model.github.GithubPullRequest;
import backend.academy.scrapper.model.github.GithubResponse;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class GithubResponseMapperTest {

    @Test
    void mapToIssue_whenBodyLongerThan200_shouldUseFirst200Symbols() {
        String issueBody = "a".repeat(210);
        GithubResponse response = new GithubResponse(
                1L,
                "IssuesEvent",
                new GithubActor("octocat"),
                OffsetDateTime.parse("2026-03-05T08:00:00Z"),
                new GithubPayload("opened", null, new GithubIssue("Issue", issueBody, List.of())));

        LinkUpdateDTO update = GithubResponseMapper.mapToIssue(response);

        assertEquals("a".repeat(200) + "...", update.descriptionPreview());
    }

    @Test
    void mapToPullRequest_whenBodyLongerThan200_shouldUseFirst200Symbols() {
        String pullRequestBody = "b".repeat(205);
        GithubResponse response = new GithubResponse(
                2L,
                "PullRequestEvent",
                new GithubActor("octocat"),
                OffsetDateTime.parse("2026-03-05T08:00:00Z"),
                new GithubPayload("opened", new GithubPullRequest("PR", pullRequestBody, List.of()), null));

        LinkUpdateDTO update = GithubResponseMapper.mapToPullRequest(response);

        assertEquals("b".repeat(200) + "...", update.descriptionPreview());
    }
}

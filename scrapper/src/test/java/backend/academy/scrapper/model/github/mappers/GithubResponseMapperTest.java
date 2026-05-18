package backend.academy.scrapper.model.github.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.github.GithubActor;
import backend.academy.scrapper.model.github.GithubCommit;
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

    @Test
    void mapToCommit_whenCommitsPresent_shouldBuildCompactSummaryDescription() {
        GithubResponse response = new GithubResponse(
                3L,
                "PushEvent",
                new GithubActor("octocat"),
                OffsetDateTime.parse("2026-03-05T08:00:00Z"),
                new GithubPayload(
                        "ignored",
                        null,
                        null,
                        "refs/heads/main",
                        "3f5c1e8e2370a49d",
                        "19fe5a1c0d2f7731",
                        List.of(
                                new GithubCommit("3f5c1e8e2370a49d", "Fix retry logic\n\nDetailed context"),
                                new GithubCommit("9a1bcdef12345678", "Add cache invalidation"))));

        LinkUpdateDTO update = GithubResponseMapper.mapToCommit(response);

        assertEquals(
                "Push в main: 2 коммита (3f5c1e8), Fix retry logic; Add cache invalidation",
                update.descriptionPreview());
    }

    @Test
    void mapToCommit_whenCommitsMissing_shouldReturnHeadFallbackWithoutTechnicalText() {
        GithubResponse response = new GithubResponse(
                4L,
                "PushEvent",
                new GithubActor("octocat"),
                OffsetDateTime.parse("2026-03-06T08:00:00Z"),
                new GithubPayload(
                        "ignored", null, null, "refs/heads/main", "3f5c1e8e2370a49d", "19fe5a1c0d2f7731", List.of()));

        LinkUpdateDTO update = GithubResponseMapper.mapToCommit(response);

        assertEquals("Push в main: HEAD 3f5c1e8 (детали коммитов недоступны)", update.descriptionPreview());
    }
}

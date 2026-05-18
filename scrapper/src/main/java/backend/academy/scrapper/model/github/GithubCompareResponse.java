package backend.academy.scrapper.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GithubCompareResponse(
        @JsonProperty("total_commits") Integer totalCommits,
        @JsonProperty("commits") List<GithubCompareCommit> commits) {}

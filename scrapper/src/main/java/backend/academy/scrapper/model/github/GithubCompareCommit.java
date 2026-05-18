package backend.academy.scrapper.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubCompareCommit(
        @JsonProperty("sha") String sha, @JsonProperty("commit") GithubCompareCommitDetails commit) {}

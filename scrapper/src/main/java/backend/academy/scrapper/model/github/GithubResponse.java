package backend.academy.scrapper.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record GithubResponse(
    @JsonProperty("id") Long id,
    @JsonProperty("type") String type,
    @JsonProperty("actor") GithubActor actor,
    @JsonProperty("created_at") OffsetDateTime creationDate,
    @JsonProperty("payload") GithubPayload payload
) {}

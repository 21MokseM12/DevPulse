package backend.academy.scrapper.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record GithubResponse(Long id, @JsonProperty("created_at") OffsetDateTime create, String type) {}

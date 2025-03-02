package backend.academy.scrapper.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record GithubResponse(@JsonProperty("created_at") OffsetDateTime create, String type) {}

package backend.academy.scrapper.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.OffsetDateTime;
import org.jetbrains.annotations.Nullable;

public record StackOverflowResponseItem(
    @JsonProperty("last_activity_date")
    OffsetDateTime lastActivity,
    @JsonProperty("creation_date")
    OffsetDateTime creationDate,
    @JsonProperty("last_edit_date")
    @Nullable
    OffsetDateTime lastEdit,
    @JsonProperty("link")
    URI postLink
) {
}

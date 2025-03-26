package backend.academy.scrapper.model.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StackOverflowOwner(
    @JsonProperty("display_name") String username
) {
}

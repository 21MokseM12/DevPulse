package backend.academy.scrapper.model.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record StackOverflowAnswerItem(
        @JsonProperty("answer_id") Long id,
        @JsonProperty("owner") StackOverflowOwner owner,
        @JsonProperty("creation_date") OffsetDateTime creationDate,
        @JsonProperty("body") String answer) {}

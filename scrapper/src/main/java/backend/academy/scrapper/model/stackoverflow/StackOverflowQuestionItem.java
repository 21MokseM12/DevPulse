package backend.academy.scrapper.model.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record StackOverflowQuestionItem(
        @JsonProperty("title") String title,
        @JsonProperty("tags") List<String> tags) {}

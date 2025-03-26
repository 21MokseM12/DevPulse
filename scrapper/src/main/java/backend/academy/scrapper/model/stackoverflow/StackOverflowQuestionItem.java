package backend.academy.scrapper.model.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StackOverflowQuestionItem(@JsonProperty("title") String title) {}

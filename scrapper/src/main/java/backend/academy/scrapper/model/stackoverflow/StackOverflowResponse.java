package backend.academy.scrapper.model.stackoverflow;

import java.util.List;

public record StackOverflowResponse<T>(List<T> items) {}

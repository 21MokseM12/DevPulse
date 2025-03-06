package backend.academy.scrapper.model;

import java.util.List;

public record StackOverflowResponse(List<StackOverflowItem> items) {
}

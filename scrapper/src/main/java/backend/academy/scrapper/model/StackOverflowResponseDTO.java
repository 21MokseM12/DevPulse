package backend.academy.scrapper.model;

import java.util.List;

public record StackOverflowResponseDTO(List<StackOverflowResponseItem> items) {
}

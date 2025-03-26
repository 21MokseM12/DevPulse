package backend.academy.scrapper.model.stackoverflow;

import backend.academy.scrapper.enums.ProcessedIdType;

public record ProcessedIdDTO(Long id, ProcessedIdType type) {}

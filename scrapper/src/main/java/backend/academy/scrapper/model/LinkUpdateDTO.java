package backend.academy.scrapper.model;

import java.time.OffsetDateTime;

public record LinkUpdateDTO(
    Long id,
    String title,
    String updateOwner,
    OffsetDateTime creationDate,
    String descriptionPreview
) {}

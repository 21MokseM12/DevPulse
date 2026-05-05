package backend.academy.scrapper.model;

import java.time.OffsetDateTime;
import java.util.Set;

public record LinkUpdateDTO(
    Long id,
    String title,
    String updateOwner,
    OffsetDateTime creationDate,
    String descriptionPreview,
    UpdateType type,
    Set<String> labels
) {
    public LinkUpdateDTO(Long id, String title, String updateOwner, OffsetDateTime creationDate, String descriptionPreview) {
        this(id, title, updateOwner, creationDate, descriptionPreview, null, Set.of());
    }
}

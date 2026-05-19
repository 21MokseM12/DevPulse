package backend.academy.scrapper.model;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Set;

public record LinkUpdateDTO(
        Long id,
        String title,
        String updateOwner,
        OffsetDateTime creationDate,
        String descriptionPreview,
        UpdateType type,
        Set<String> labels,
        URI eventUrl) {
    public LinkUpdateDTO(
            Long id, String title, String updateOwner, OffsetDateTime creationDate, String descriptionPreview) {
        this(id, title, updateOwner, creationDate, descriptionPreview, null, Set.of(), null);
    }

    public LinkUpdateDTO(
            Long id,
            String title,
            String updateOwner,
            OffsetDateTime creationDate,
            String descriptionPreview,
            UpdateType type,
            Set<String> labels) {
        this(id, title, updateOwner, creationDate, descriptionPreview, type, labels, null);
    }
}

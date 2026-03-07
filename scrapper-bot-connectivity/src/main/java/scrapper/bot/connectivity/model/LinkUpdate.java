package scrapper.bot.connectivity.model;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

public record LinkUpdate(
    Long id,
    URI url,
    String title,
    String updateOwner,
    String description,
    OffsetDateTime creationDate,
    List<Long> tgChatIds
) {
}

package scrapper.bot.connectivity.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import scrapper.bot.connectivity.validation.ValidUri;

public record LinkUpdate(
        @NotNull Long id,
        @NotNull @ValidUri URI url,
        @NotBlank String title,
        @NotBlank String updateOwner,
        @NotBlank String description,
        @NotNull OffsetDateTime creationDate,
        @NotNull List<@NotNull Long> clientsIds) {}

package scrapper.bot.connectivity.model.request;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import scrapper.bot.connectivity.validation.ValidUri;

public record RemoveLinkRequest(@NotNull @ValidUri URI link) {}

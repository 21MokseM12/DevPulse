package scrapper.bot.connectivity.model.request;

import jakarta.validation.constraints.NotBlank;

public record ClientCredentialsRequest(@NotBlank String login, @NotBlank String password) {}

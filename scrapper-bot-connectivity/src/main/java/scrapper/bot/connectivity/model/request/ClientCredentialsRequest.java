package scrapper.bot.connectivity.model.request;

public record ClientCredentialsRequest(
    String login,
    String password
) {
}

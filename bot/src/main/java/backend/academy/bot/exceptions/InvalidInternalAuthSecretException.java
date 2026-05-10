package backend.academy.bot.exceptions;

public class InvalidInternalAuthSecretException extends RuntimeException {
    public InvalidInternalAuthSecretException(String message) {
        super(message);
    }
}

package backend.academy.bot.exceptions;

public class MissingInternalAuthHeaderException extends RuntimeException {
    public MissingInternalAuthHeaderException(String message) {
        super(message);
    }
}

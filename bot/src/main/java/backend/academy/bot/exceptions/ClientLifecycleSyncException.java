package backend.academy.bot.exceptions;

public class ClientLifecycleSyncException extends RuntimeException {
    public ClientLifecycleSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}

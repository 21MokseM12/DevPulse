package backend.academy.bot.model.commands;

public class InvalidCommandException extends RuntimeException {
    public InvalidCommandException(String message) {super(message);}
}

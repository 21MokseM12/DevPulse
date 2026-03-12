package backend.academy.scrapper.exceptions;

public class KafkaProcessException extends RuntimeException {
    public KafkaProcessException(String message) {
        super(message);
    }
}

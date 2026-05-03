package backend.academy.scrapper.db.model;

public record KafkaOutboxMessage(long id, String topic, String payload) {}

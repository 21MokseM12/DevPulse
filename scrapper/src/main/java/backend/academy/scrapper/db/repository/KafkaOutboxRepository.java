package backend.academy.scrapper.db.repository;

import scrapper.bot.connectivity.model.LinkUpdate;

public interface KafkaOutboxRepository {
    void save(String topic, LinkUpdate payload);
}

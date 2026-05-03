package backend.academy.scrapper.db.repository;

import backend.academy.scrapper.db.model.KafkaOutboxMessage;
import java.time.LocalDateTime;
import java.util.List;
import scrapper.bot.connectivity.model.LinkUpdate;

public interface KafkaOutboxRepository {
    void save(String topic, LinkUpdate payload);

    List<KafkaOutboxMessage> findPendingBatch(int batchSize);

    void markSent(long id, LocalDateTime sentAt, int attempts);

    void incrementAttemptCount(long id, int attempts);

    long countPending();
}

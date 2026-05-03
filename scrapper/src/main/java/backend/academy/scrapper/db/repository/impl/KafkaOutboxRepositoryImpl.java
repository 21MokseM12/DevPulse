package backend.academy.scrapper.db.repository.impl;

import backend.academy.scrapper.db.model.KafkaOutboxMessage;
import backend.academy.scrapper.db.query.KafkaOutboxQuery;
import backend.academy.scrapper.db.repository.KafkaOutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import scrapper.bot.connectivity.model.LinkUpdate;

@Repository
@RequiredArgsConstructor
public class KafkaOutboxRepositoryImpl implements KafkaOutboxRepository {

    private static final String TOPIC = "topic";
    private static final String PAYLOAD = "payload";
    private static final String CREATED_AT = "createdAt";
    private static final String BATCH_SIZE = "batchSize";
    private static final String ID = "id";
    private static final String SENT_AT = "sentAt";
    private static final String ATTEMPTS = "attempts";

    private final ObjectMapper objectMapper;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void save(String topic, LinkUpdate payload) {
        try {
            jdbcTemplate.update(
                KafkaOutboxQuery.INSERT.query(),
                new MapSqlParameterSource()
                    .addValue(TOPIC, topic)
                    .addValue(PAYLOAD, objectMapper.writeValueAsString(payload))
                    .addValue(CREATED_AT, OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime())
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize outbox payload", e);
        }
    }

    @Override
    public List<KafkaOutboxMessage> findPendingBatch(int batchSize) {
        return jdbcTemplate.query(
                KafkaOutboxQuery.SELECT_PENDING_BATCH.query(),
                new MapSqlParameterSource().addValue(BATCH_SIZE, batchSize),
                (rs, _) -> new KafkaOutboxMessage(
                        rs.getLong(ID),
                        rs.getString(TOPIC),
                        rs.getString(PAYLOAD)));
    }

    @Override
    public void markSent(long id, LocalDateTime sentAt, int attempts) {
        jdbcTemplate.update(
                KafkaOutboxQuery.MARK_SENT.query(),
                new MapSqlParameterSource()
                        .addValue(ID, id)
                        .addValue(SENT_AT, sentAt)
                        .addValue(ATTEMPTS, attempts));
    }

    @Override
    public void incrementAttemptCount(long id, int attempts) {
        jdbcTemplate.update(
                KafkaOutboxQuery.INCREMENT_ATTEMPT_COUNT.query(),
                new MapSqlParameterSource()
                        .addValue(ID, id)
                        .addValue(ATTEMPTS, attempts));
    }

    @Override
    public long countPending() {
        Long count = jdbcTemplate.queryForObject(KafkaOutboxQuery.COUNT_PENDING.query(), new MapSqlParameterSource(), Long.class);
        return count == null ? 0L : count;
    }
}

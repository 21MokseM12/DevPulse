package backend.academy.scrapper.db.repository.impl;

import backend.academy.scrapper.db.query.KafkaOutboxQuery;
import backend.academy.scrapper.db.repository.KafkaOutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
}

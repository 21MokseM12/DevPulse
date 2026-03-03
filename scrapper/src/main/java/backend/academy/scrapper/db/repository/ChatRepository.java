package backend.academy.scrapper.db.repository;

import backend.academy.scrapper.db.query.ChatQuery;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ChatRepository {

    private static final String ID = "id";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public boolean isClient(Long id) {
        return Optional.ofNullable(
            jdbcTemplate.queryForObject(
                ChatQuery.SELECT_COUNT_BY_ID.query(),
                new MapSqlParameterSource()
                    .addValue(ID, id),
                Integer.class
            )
        ).orElse(0) > 0;
    }

    @Transactional
    public void save(Long id) {
        jdbcTemplate.query(
            ChatQuery.INSERT_CHAT.query(),
            new MapSqlParameterSource()
                .addValue(ID, id),
            new RowCountCallbackHandler()
        );
    }

    @Transactional
    public boolean delete(Long id) {
        return jdbcTemplate.update(
            ChatQuery.DELETE_BY_ID.query(),
            new MapSqlParameterSource()
                .addValue(ID, id)
        ) > 0;
    }
}

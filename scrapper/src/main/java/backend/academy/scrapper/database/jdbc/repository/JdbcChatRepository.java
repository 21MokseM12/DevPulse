package backend.academy.scrapper.database.jdbc.repository;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JdbcChatRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public boolean isClient(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        Integer countChats =
                jdbcTemplate.queryForObject("select count(id) from chats where id = :id", params, Integer.class);
        return Optional.ofNullable(countChats).orElse(0) > 0;
    }

    @Transactional
    public boolean save(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        RowCountCallbackHandler countCallbackHandler = new RowCountCallbackHandler();
        jdbcTemplate.query("insert into chats (id) values (:id)", params, countCallbackHandler);
        return countCallbackHandler.getRowCount() != 0;
    }

    @Transactional
    public boolean delete(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        RowCountCallbackHandler countCallbackHandler = new RowCountCallbackHandler();
        jdbcTemplate.query("delete from chats where id = :id", params, countCallbackHandler);
        return countCallbackHandler.getRowCount() != 0;
    }
}

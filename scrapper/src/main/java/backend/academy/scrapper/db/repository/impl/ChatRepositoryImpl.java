package backend.academy.scrapper.db.repository.impl;

import backend.academy.scrapper.db.query.ChatQuery;
import backend.academy.scrapper.db.repository.ChatRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ChatRepositoryImpl implements ChatRepository {

    private static final String ID = "id";
    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public boolean existsByLogin(String login) {
        return Optional.ofNullable(
            jdbcTemplate.queryForObject(
                ChatQuery.SELECT_COUNT_BY_LOGIN.query(),
                new MapSqlParameterSource().addValue(LOGIN, login),
                Integer.class
            )
        ).orElse(0) > 0;
    }

    @Override
    public boolean isClient(String login, String password) {
        return Optional.ofNullable(
            jdbcTemplate.queryForObject(
                ChatQuery.SELECT_COUNT_BY_CREDENTIALS.query(),
                new MapSqlParameterSource()
                    .addValue(LOGIN, login)
                    .addValue(PASSWORD, password),
                Integer.class
            )
        ).orElse(0) > 0;
    }

    @Override
    public Optional<Long> findIdByCredentials(String login, String password) {
        return Optional.ofNullable(
            jdbcTemplate.queryForObject(
                ChatQuery.SELECT_ID_BY_CREDENTIALS.query(),
                new MapSqlParameterSource()
                    .addValue(LOGIN, login)
                    .addValue(PASSWORD, password),
                Long.class
            )
        );
    }

    @Override
    @Transactional
    public void save(String login, String password) {
        jdbcTemplate.update(
            ChatQuery.INSERT_CHAT_BY_CREDENTIALS.query(),
            new MapSqlParameterSource()
                .addValue(LOGIN, login)
                .addValue(PASSWORD, password)
        );
    }

    @Override
    @Transactional
    public boolean delete(String login, String password) {
        return jdbcTemplate.update(
            ChatQuery.DELETE_BY_CREDENTIALS.query(),
            new MapSqlParameterSource()
                .addValue(LOGIN, login)
                .addValue(PASSWORD, password)
        ) > 0;
    }

    @Override
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

    @Override
    @Transactional
    public void save(Long id) {
        jdbcTemplate.update(
            ChatQuery.INSERT_CHAT.query(),
            new MapSqlParameterSource()
                .addValue(ID, id)
        );
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return jdbcTemplate.update(
            ChatQuery.DELETE_BY_ID.query(),
            new MapSqlParameterSource()
                .addValue(ID, id)
        ) > 0;
    }
}

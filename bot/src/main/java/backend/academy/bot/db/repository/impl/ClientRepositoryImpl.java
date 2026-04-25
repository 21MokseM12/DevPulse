package backend.academy.bot.db.repository.impl;

import backend.academy.bot.db.model.Client;
import backend.academy.bot.db.repository.ClientRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClientRepositoryImpl implements ClientRepository {

    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";

    private static final String SELECT_BY_LOGIN = "SELECT id, login, password FROM clients WHERE login = :login";
    private static final String INSERT =
            "INSERT INTO clients(login, password) VALUES(:login, :password) RETURNING id";
    private static final String DELETE_BY_LOGIN = "DELETE FROM clients WHERE login = :login";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<Client> CLIENT_ROW_MAPPER =
            (rs, rowNum) -> new Client(rs.getLong("id"), rs.getString(LOGIN), rs.getString(PASSWORD));

    @Override
    public Optional<Client> findByLogin(String login) {
        var client = jdbcTemplate.query(
                SELECT_BY_LOGIN,
                new MapSqlParameterSource().addValue(LOGIN, login),
                CLIENT_ROW_MAPPER);
        return client.stream().findFirst();
    }

    @Override
    public long save(String login, String password) {
        Long id = jdbcTemplate.queryForObject(
                INSERT,
                new MapSqlParameterSource().addValue(LOGIN, login).addValue(PASSWORD, password),
                Long.class);
        return Optional.ofNullable(id).orElseThrow();
    }

    @Override
    public boolean deleteByLogin(String login) {
        return jdbcTemplate.update(DELETE_BY_LOGIN, new MapSqlParameterSource().addValue(LOGIN, login)) == 1;
    }
}

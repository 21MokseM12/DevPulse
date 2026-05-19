package backend.academy.bot.db.repository.impl;

import backend.academy.bot.db.model.PushPlatform;
import backend.academy.bot.db.model.PushToken;
import backend.academy.bot.db.model.PushTokenStatus;
import backend.academy.bot.db.repository.PushTokenRepository;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PushTokenRepositoryImpl implements PushTokenRepository {
    private static final String CLIENT_LOGIN = "client_login";
    private static final String PLATFORM = "platform";
    private static final String TOKEN = "token";
    private static final String APP_VERSION = "app_version";
    private static final String DEVICE_ID = "device_id";
    private static final String STATUS = "status";
    private static final String TOKEN_ID = "token_id";

    private static final String UPSERT =
            """
            INSERT INTO push_tokens(client_login, platform, token, app_version, device_id, status, updated_at, last_seen_at)
            VALUES(:client_login, :platform, :token, :app_version, :device_id, 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (platform, token) DO UPDATE
            SET client_login = EXCLUDED.client_login,
                app_version = EXCLUDED.app_version,
                device_id = EXCLUDED.device_id,
                status = 'active',
                updated_at = CURRENT_TIMESTAMP,
                last_seen_at = CURRENT_TIMESTAMP
            RETURNING id, client_login, platform, token, app_version, device_id, status, created_at, updated_at, last_seen_at
            """;

    private static final String DEACTIVATE =
            """
            UPDATE push_tokens
            SET status = 'inactive',
                updated_at = CURRENT_TIMESTAMP
            WHERE client_login = :client_login
              AND platform = :platform
              AND token = :token
              AND status = 'active'
            """;

    private static final String FIND_ACTIVE_BY_CLIENT =
            """
            SELECT id, client_login, platform, token, app_version, device_id, status, created_at, updated_at, last_seen_at
            FROM push_tokens
            WHERE client_login = :client_login
              AND status = 'active'
            ORDER BY id
            """;

    private static final String MARK_INVALID =
            """
            UPDATE push_tokens
            SET status = 'invalid',
                updated_at = CURRENT_TIMESTAMP
            WHERE id = :token_id
              AND status <> 'invalid'
            """;

    private static final String COUNT_ACTIVE_BY_CLIENT =
            """
            SELECT COUNT(*)
            FROM push_tokens
            WHERE client_login = :client_login
              AND status = 'active'
            """;

    private static final RowMapper<PushToken> ROW_MAPPER = (rs, rowNum) -> new PushToken(
            rs.getLong("id"),
            rs.getString(CLIENT_LOGIN),
            PushPlatform.valueOf(rs.getString(PLATFORM).toUpperCase()),
            rs.getString(TOKEN),
            rs.getString(APP_VERSION),
            rs.getString(DEVICE_ID),
            PushTokenStatus.valueOf(rs.getString(STATUS).toUpperCase()),
            toOffsetDateTime(rs.getTimestamp("created_at")),
            toOffsetDateTime(rs.getTimestamp("updated_at")),
            toOffsetDateTime(rs.getTimestamp("last_seen_at")));

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public PushToken upsert(
            String clientLogin, PushPlatform platform, String token, String appVersion, String deviceId) {
        return jdbcTemplate.queryForObject(
                UPSERT,
                new MapSqlParameterSource()
                        .addValue(CLIENT_LOGIN, clientLogin)
                        .addValue(PLATFORM, platform.dbValue())
                        .addValue(TOKEN, token)
                        .addValue(APP_VERSION, appVersion)
                        .addValue(DEVICE_ID, deviceId),
                ROW_MAPPER);
    }

    @Override
    public boolean deactivate(String clientLogin, PushPlatform platform, String token) {
        int updated = jdbcTemplate.update(
                DEACTIVATE,
                new MapSqlParameterSource()
                        .addValue(CLIENT_LOGIN, clientLogin)
                        .addValue(PLATFORM, platform.dbValue())
                        .addValue(TOKEN, token));
        return updated > 0;
    }

    @Override
    public List<PushToken> findActiveByClientLogin(String clientLogin) {
        return jdbcTemplate.query(
                FIND_ACTIVE_BY_CLIENT, new MapSqlParameterSource().addValue(CLIENT_LOGIN, clientLogin), ROW_MAPPER);
    }

    @Override
    public boolean markInvalid(long tokenId) {
        int updated = jdbcTemplate.update(MARK_INVALID, new MapSqlParameterSource().addValue(TOKEN_ID, tokenId));
        return updated > 0;
    }

    @Override
    public long countActiveByClientLogin(String clientLogin) {
        Long count = jdbcTemplate.queryForObject(
                COUNT_ACTIVE_BY_CLIENT, new MapSqlParameterSource().addValue(CLIENT_LOGIN, clientLogin), Long.class);
        return count == null ? 0L : count;
    }

    private static OffsetDateTime toOffsetDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }
}

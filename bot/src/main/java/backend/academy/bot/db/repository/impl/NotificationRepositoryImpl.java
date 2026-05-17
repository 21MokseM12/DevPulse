package backend.academy.bot.db.repository.impl;

import backend.academy.bot.db.model.Notification;
import backend.academy.bot.db.model.NotificationView;
import backend.academy.bot.db.repository.NotificationRepository;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private static final String LINK_ID = "link_id";
    private static final String URL = "url";
    private static final String TITLE = "title";
    private static final String UPDATE_OWNER = "update_owner";
    private static final String DESCRIPTION = "description";
    private static final String CREATION_DATE = "creation_date";
    private static final String RECEIVED_AT = "received_at";
    private static final String READ_AT = "read_at";
    private static final String NOTIFICATION_ID = "notification_id";
    private static final String CLIENT_LOGIN = "client_login";
    private static final String CLIENT_ID = "client_id";
    private static final String LIMIT = "limit";
    private static final String OFFSET = "offset";
    private static final String URLS = "urls";
    private static final String IDS = "ids";

    private static final String INSERT =
            """
            INSERT INTO notifications(link_id, url, title, update_owner, description, creation_date)
            VALUES(:link_id, :url, :title, :update_owner, :description, :creation_date)
            ON CONFLICT (link_id, creation_date, update_owner, title) DO NOTHING
            RETURNING id
            """;
    private static final String SELECT_EXISTING_ID =
            """
            SELECT id
            FROM notifications
            WHERE link_id = :link_id
              AND creation_date = :creation_date
              AND update_owner = :update_owner
              AND title = :title
            """;
    private static final String INSERT_RECIPIENT =
            """
            INSERT INTO notification_recipients(notification_id, client_login)
            SELECT :notification_id, c.login
            FROM clients c
            WHERE c.id = :client_id
            ON CONFLICT DO NOTHING
            """;
    private static final String SELECT_CLIENT_LOGIN_BY_ID =
            """
            SELECT login
            FROM clients
            WHERE id = :client_id
            """;
    private static final String SELECT_BY_CLIENT =
            """
            SELECT n.id, n.link_id, n.url, n.title, n.update_owner, n.description, n.creation_date, n.received_at, nr.read_at
            FROM notifications n
            JOIN notification_recipients nr ON nr.notification_id = n.id
            WHERE nr.client_login = :client_login
            ORDER BY n.received_at DESC, n.id DESC
            LIMIT :limit OFFSET :offset
            """;
    private static final String SELECT_BY_CLIENT_AND_URLS =
            """
            SELECT n.id, n.link_id, n.url, n.title, n.update_owner, n.description, n.creation_date, n.received_at, nr.read_at
            FROM notifications n
            JOIN notification_recipients nr ON nr.notification_id = n.id
            WHERE nr.client_login = :client_login
              AND n.url IN (:urls)
            ORDER BY n.received_at DESC, n.id DESC
            LIMIT :limit OFFSET :offset
            """;
    private static final String COUNT_UNREAD_BY_CLIENT =
            """
            SELECT COUNT(*)
            FROM notification_recipients
            WHERE client_login = :client_login
              AND read_at IS NULL
            """;
    private static final String MARK_READ_BY_IDS =
            """
            UPDATE notification_recipients
            SET read_at = :read_at
            WHERE client_login = :client_login
              AND notification_id IN (:ids)
              AND read_at IS NULL
            """;
    private static final String MARK_ALL_READ =
            """
            UPDATE notification_recipients
            SET read_at = :read_at
            WHERE client_login = :client_login
              AND read_at IS NULL
            """;
    private static final RowMapper<NotificationView> NOTIFICATION_VIEW_ROW_MAPPER =
            (rs, rowNum) -> new NotificationView(
                    rs.getLong("id"),
                    rs.getLong(LINK_ID),
                    rs.getString(URL),
                    rs.getString(TITLE),
                    rs.getString(UPDATE_OWNER),
                    rs.getString(DESCRIPTION),
                    toOffsetDateTime(rs.getTimestamp(CREATION_DATE)),
                    toOffsetDateTime(rs.getTimestamp(RECEIVED_AT)),
                    toOffsetDateTime(rs.getTimestamp(READ_AT)));

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public long save(Notification notification) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LINK_ID, notification.linkId())
                .addValue(URL, notification.url())
                .addValue(TITLE, notification.title())
                .addValue(UPDATE_OWNER, notification.updateOwner())
                .addValue(DESCRIPTION, notification.description())
                .addValue(CREATION_DATE, notification.creationDate());
        Long id = jdbcTemplate.queryForObject(INSERT, params, Long.class);
        long notificationId = Optional.ofNullable(id).orElseGet(() -> Optional.ofNullable(
                        jdbcTemplate.queryForObject(SELECT_EXISTING_ID, params, Long.class))
                .orElseThrow());
        saveRecipients(notificationId, notification.clientsIds());
        return notificationId;
    }

    private void saveRecipients(long notificationId, List<Long> clientsIds) {
        clientsIds.forEach(clientId -> {
            String clientLogin;
            try {
                clientLogin = jdbcTemplate.queryForObject(
                        SELECT_CLIENT_LOGIN_BY_ID,
                        new MapSqlParameterSource().addValue(CLIENT_ID, clientId),
                        String.class);
            } catch (EmptyResultDataAccessException ex) {
                clientLogin = null;
            }
            if (clientLogin == null || clientLogin.isBlank()) {
                log.warn("Skip notification recipient: client id {} was not found in clients table", clientId);
                return;
            }
            jdbcTemplate.update(
                    INSERT_RECIPIENT,
                    new MapSqlParameterSource()
                            .addValue(NOTIFICATION_ID, notificationId)
                            .addValue(CLIENT_ID, clientId));
        });
    }

    @Override
    public List<NotificationView> findByClientLogin(String clientLogin, int limit, int offset) {
        return jdbcTemplate.query(
                SELECT_BY_CLIENT,
                new MapSqlParameterSource()
                        .addValue(CLIENT_LOGIN, clientLogin)
                        .addValue(LIMIT, limit)
                        .addValue(OFFSET, offset),
                NOTIFICATION_VIEW_ROW_MAPPER);
    }

    @Override
    public List<NotificationView> findByClientLoginAndUrls(
            String clientLogin, Set<String> urls, int limit, int offset) {
        if (urls.isEmpty()) {
            return List.of();
        }
        return jdbcTemplate.query(
                SELECT_BY_CLIENT_AND_URLS,
                new MapSqlParameterSource()
                        .addValue(CLIENT_LOGIN, clientLogin)
                        .addValue(URLS, urls)
                        .addValue(LIMIT, limit)
                        .addValue(OFFSET, offset),
                NOTIFICATION_VIEW_ROW_MAPPER);
    }

    @Override
    public long countUnreadByClientLogin(String clientLogin) {
        Long count = jdbcTemplate.queryForObject(
                COUNT_UNREAD_BY_CLIENT, new MapSqlParameterSource().addValue(CLIENT_LOGIN, clientLogin), Long.class);
        return Optional.ofNullable(count).orElse(0L);
    }

    @Override
    public long markAsReadByIds(String clientLogin, Set<Long> notificationIds, OffsetDateTime readAt) {
        if (notificationIds.isEmpty()) {
            return 0L;
        }
        return jdbcTemplate.update(
                MARK_READ_BY_IDS,
                new MapSqlParameterSource()
                        .addValue(CLIENT_LOGIN, clientLogin)
                        .addValue(IDS, notificationIds)
                        .addValue(READ_AT, readAt));
    }

    @Override
    public long markAllAsRead(String clientLogin, OffsetDateTime readAt) {
        return jdbcTemplate.update(
                MARK_ALL_READ,
                new MapSqlParameterSource().addValue(CLIENT_LOGIN, clientLogin).addValue(READ_AT, readAt));
    }

    private static OffsetDateTime toOffsetDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }
}

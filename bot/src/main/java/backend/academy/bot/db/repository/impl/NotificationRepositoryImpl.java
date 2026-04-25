package backend.academy.bot.db.repository.impl;

import backend.academy.bot.db.model.Notification;
import backend.academy.bot.db.repository.NotificationRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private static final String LINK_ID = "link_id";
    private static final String URL = "url";
    private static final String TITLE = "title";
    private static final String UPDATE_OWNER = "update_owner";
    private static final String DESCRIPTION = "description";
    private static final String CREATION_DATE = "creation_date";
    private static final String NOTIFICATION_ID = "notification_id";
    private static final String CLIENT_LOGIN = "client_login";

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
            SELECT :notification_id, :client_login
            WHERE EXISTS (SELECT 1 FROM clients WHERE login = :client_login)
            ON CONFLICT DO NOTHING
            """;

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
        Long id = jdbcTemplate.queryForObject(
                INSERT,
                params,
                Long.class);
        long notificationId = Optional.ofNullable(id).orElseGet(() -> Optional.ofNullable(
                        jdbcTemplate.queryForObject(SELECT_EXISTING_ID, params, Long.class))
                .orElseThrow());
        saveRecipients(notificationId, notification.clientsIds());
        return notificationId;
    }

    private void saveRecipients(long notificationId, List<Long> clientsIds) {
        clientsIds.stream()
                .map(String::valueOf)
                .forEach(clientLogin -> jdbcTemplate.update(
                        INSERT_RECIPIENT,
                        new MapSqlParameterSource()
                                .addValue(NOTIFICATION_ID, notificationId)
                                .addValue(CLIENT_LOGIN, clientLogin)));
    }
}

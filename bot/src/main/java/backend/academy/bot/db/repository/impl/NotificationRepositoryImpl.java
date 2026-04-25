package backend.academy.bot.db.repository.impl;

import backend.academy.bot.db.model.Notification;
import backend.academy.bot.db.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final String CLIENTS_IDS = "clients_ids";

    private static final String INSERT =
            """
            INSERT INTO notifications(link_id, url, title, update_owner, description, creation_date, clients_ids)
            VALUES(:link_id, :url, :title, :update_owner, :description, :creation_date, :clients_ids)
            RETURNING id
            """;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public long save(Notification notification) {
        Long id = jdbcTemplate.queryForObject(
                INSERT,
                new MapSqlParameterSource()
                        .addValue(LINK_ID, notification.linkId())
                        .addValue(URL, notification.url())
                        .addValue(TITLE, notification.title())
                        .addValue(UPDATE_OWNER, notification.updateOwner())
                        .addValue(DESCRIPTION, notification.description())
                        .addValue(CREATION_DATE, notification.creationDate())
                        .addValue(CLIENTS_IDS, toJson(notification.clientsIds())),
                Long.class);
        return Optional.ofNullable(id).orElseThrow();
    }

    private String toJson(List<Long> clientsIds) {
        try {
            return OBJECT_MAPPER.writeValueAsString(clientsIds);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize clients ids", e);
        }
    }
}

package backend.academy.scrapper.db.repository;

import backend.academy.scrapper.db.model.Link;
import backend.academy.scrapper.db.query.LinkQuery;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LinkRepository {

    private static final String LINK = "link";
    private static final String UPDATED_AT = "updated_at";
    private static final String LINK_ID = "link_id";
    private static final String TIME_LIMIT = "highestTimeLimit";
    private static final String OFFSET = "offset";
    private static final String LIMIT = "limit";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Link> rowMapper;

    public Long save(String link, OffsetDateTime createdTime) {
        return jdbcTemplate.queryForObject(
            LinkQuery.INSERT.query(),
            new MapSqlParameterSource()
                .addValue(LINK, link)
                .addValue(UPDATED_AT, createdTime),
            Long.class
        );
    }

    public Optional<Long> findIdByLink(String link) {
        return Optional.ofNullable(jdbcTemplate.queryForObject(
            LinkQuery.SELECT_BY_LINK.query(),
            new MapSqlParameterSource()
                .addValue(LINK, link),
            Long.class
        ));
    }

    public Optional<Link> findById(Long linkId) {
        return Optional.ofNullable(jdbcTemplate.queryForObject(
            LinkQuery.SELECT_BY_ID.query(),
            new MapSqlParameterSource()
                .addValue(LINK_ID, linkId),
            rowMapper
        ));
    }

    public boolean existsLink(String link) {
        return Optional.ofNullable(jdbcTemplate.queryForObject(
            LinkQuery.SELECT_COUNT_BY_LINK.query(),
            new MapSqlParameterSource()
                .addValue(LINK, link),
            Integer.class
        )).orElse(0) == 1;
    }

    public Optional<Link> delete(Long id) {
        return Optional.ofNullable(jdbcTemplate.queryForObject(
            LinkQuery.DELETE_BY_ID.query(),
            new MapSqlParameterSource()
                .addValue(LINK_ID, id),
            rowMapper
        ));
    }

    public Set<URI> findAllLinksByUpdatedAt(OffsetDateTime highestTimeLimit, int offset, Integer limit) {
        return jdbcTemplate.queryForList(
                LinkQuery.SELECT_BY_UPDATED_AT.query(),
                new MapSqlParameterSource()
                    .addValue(TIME_LIMIT, highestTimeLimit.toLocalDateTime())
                    .addValue(OFFSET, offset)
                    .addValue(LIMIT, limit),
                String.class
            ).stream()
            .map(URI::create)
            .collect(Collectors.toSet());
    }
}

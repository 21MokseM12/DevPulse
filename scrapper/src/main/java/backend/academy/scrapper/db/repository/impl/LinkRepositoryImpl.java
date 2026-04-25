package backend.academy.scrapper.db.repository.impl;

import backend.academy.scrapper.db.model.Link;
import backend.academy.scrapper.db.query.LinkQuery;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import backend.academy.scrapper.db.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LinkRepositoryImpl implements LinkRepository {

    private static final String URL = "url";
    private static final String LAST_CHECKED_AT = "last_checked_at";
    private static final String CREATED_AT = "created_at";
    private static final String LINK_TYPE = "link_type";
    private static final String ETAG = "etag";
    private static final String LINK_ID = "link_id";
    private static final String TIME_LIMIT = "highestTimeLimit";
    private static final String OFFSET = "offset";
    private static final String LIMIT = "limit";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Link> rowMapper;

    public Long save(String url, OffsetDateTime createdTime) {
        return jdbcTemplate.queryForObject(
            LinkQuery.INSERT.query(),
            new MapSqlParameterSource()
                .addValue(URL, url)
                .addValue(LAST_CHECKED_AT, createdTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime())
                .addValue(CREATED_AT, createdTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime())
                .addValue(LINK_TYPE, resolveLinkType(url))
                .addValue(ETAG, null),
            Long.class
        );
    }

    public Optional<Link> findIdByLink(String url) {
        return Optional.ofNullable(jdbcTemplate.queryForObject(
            LinkQuery.SELECT_BY_LINK.query(),
            new MapSqlParameterSource()
                .addValue(URL, url),
            rowMapper
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

    public boolean existsLink(String url) {
        return Optional.ofNullable(jdbcTemplate.queryForObject(
            LinkQuery.SELECT_COUNT_BY_LINK.query(),
            new MapSqlParameterSource()
                .addValue(URL, url),
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
                    .addValue(TIME_LIMIT, highestTimeLimit.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime())
                    .addValue(OFFSET, offset)
                    .addValue(LIMIT, limit),
                String.class
            ).stream()
            .map(URI::create)
            .collect(Collectors.toSet());
    }

    private String resolveLinkType(String url) {
        if (url.startsWith("https://github.com/")) {
            return "GITHUB";
        }
        if (url.startsWith("https://stackoverflow.com/")) {
            return "STACKOVERFLOW";
        }
        return "UNKNOWN";
    }
}

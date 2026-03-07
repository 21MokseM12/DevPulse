package backend.academy.scrapper.db.repository.impl;

import java.util.HashSet;
import java.util.Set;
import backend.academy.scrapper.db.query.FilterQuery;
import backend.academy.scrapper.db.repository.FilterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class FilterRepositoryImpl implements FilterRepository {

    private static final String FILTER = "filter";
    private static final String LINK_ID = "link_id";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.MANDATORY)
    public void save(Set<String> filters, Long linkId) {
        MapSqlParameterSource[] filterSources = filters.stream()
            .map(filter -> new MapSqlParameterSource()
                .addValue(FILTER, filter)
                .addValue(LINK_ID, linkId)
            )
            .toArray(MapSqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(
            FilterQuery.INSERT_BATCH.query(),
            filterSources
        );
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Set<String> findByLinkId(Long linkId) {
        return new HashSet<>(jdbcTemplate.queryForList(
            FilterQuery.SELECT_BY_LINK_ID.query(),
            new MapSqlParameterSource()
                .addValue(LINK_ID, linkId),
            String.class
        ));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Set<String> deleteByLinkId(Long linkId) {
        Set<String> deletedFilters = new HashSet<>();
        jdbcTemplate.query(
            FilterQuery.DELETE_BY_LINK_ID.query(),
            new MapSqlParameterSource()
                .addValue(LINK_ID, linkId),
            rs -> {
                deletedFilters.add(rs.getString(FILTER));
            }
        );
        return deletedFilters;
    }
}

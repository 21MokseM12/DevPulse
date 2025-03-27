package backend.academy.scrapper.database.jdbc.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JdbcFilterRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.MANDATORY)
    public void save(Set<String> filters, Long linkId) {
        List<MapSqlParameterSource> filterSources = new ArrayList<>();
        for (String filter : filters) {
            MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
            mapSqlParameterSource.addValue("filter", filter);
            mapSqlParameterSource.addValue("link_id", linkId);
            filterSources.add(mapSqlParameterSource);
        }
        jdbcTemplate.batchUpdate(
                "insert into filters (filter, link_id) values (:filter, :link_id)",
                filterSources.toArray(new MapSqlParameterSource[] {}));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Set<String> findByLinkId(Long linkId) {
        String query = "select filter from filters where link_id = :link_id";

        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("link_id", linkId);

        return new HashSet<>(jdbcTemplate.queryForList(query, mapSqlParameterSource, String.class));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Set<String> deleteByLinkId(Long linkId) {
        Set<String> deletedFilters = new HashSet<>();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("link_id", linkId);

        jdbcTemplate.query("delete from filters where link_id = :link_id returning filter", params, rs -> {
            deletedFilters.add(rs.getString("filter"));
        });

        return deletedFilters;
    }
}

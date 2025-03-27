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
public class JdbcTagRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.MANDATORY)
    public void save(Set<String> tags, Long linkId) {
        List<MapSqlParameterSource> tagSources = new ArrayList<>();
        for (String tag : tags) {
            MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
            mapSqlParameterSource.addValue("tag", tag);
            mapSqlParameterSource.addValue("link_id", linkId);
            tagSources.add(mapSqlParameterSource);
        }
        jdbcTemplate.batchUpdate(
                "insert into tags (tag, link_id) values (:tag, :link_id)",
                tagSources.toArray(new MapSqlParameterSource[] {}));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Set<String> findByLinkId(Long linkId) {
        String query = "select tag from tags where link_id = :link_id";

        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("link_id", linkId);

        return new HashSet<>(jdbcTemplate.queryForList(query, mapSqlParameterSource, String.class));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Set<String> deleteByLinkId(Long linkId) {
        Set<String> deletedTags = new HashSet<>();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("link_id", linkId);

        jdbcTemplate.query("delete from tags where link_id = :link_id returning tag", params, rs -> {
            deletedTags.add(rs.getString("tag"));
        });

        return deletedTags;
    }
}

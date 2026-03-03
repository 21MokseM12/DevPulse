package backend.academy.scrapper.db.repository;

import backend.academy.scrapper.db.query.TagQuery;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class TagRepository {

    private static final String LINK_ID = "link_id";
    private static final String TAG = "tag";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.MANDATORY)
    public void save(Set<String> tags, Long linkId) {
        MapSqlParameterSource[] tagSources = tags.stream()
            .map(tag -> new MapSqlParameterSource()
                .addValue(TAG, tag)
                .addValue(LINK_ID, linkId)
            ).toArray(MapSqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(TagQuery.INSERT.query(), tagSources);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Set<String> findByLinkId(Long linkId) {
        return new HashSet<>(jdbcTemplate.queryForList(
            TagQuery.SELECT_BY_LINK_ID.query(),
            new MapSqlParameterSource()
                .addValue(LINK_ID, linkId),
            String.class
        ));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Set<String> deleteByLinkId(Long linkId) {
        Set<String> deletedTags = new HashSet<>();
        jdbcTemplate.query(
            TagQuery.DELETE_BY_LINK_ID.query(),
            new MapSqlParameterSource()
                .addValue(LINK_ID, linkId),
            rs -> {deletedTags.add(rs.getString(TAG));}
        );
        return deletedTags;
    }
}

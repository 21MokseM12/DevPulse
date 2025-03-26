package backend.academy.scrapper.database.jdbc.repository;

import backend.academy.scrapper.database.jdbc.model.ProcessedId;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcProcessedIdRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Set<ProcessedId> findAll(Long linkId) {
        Set<ProcessedId> resultList = new HashSet<>();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("linkId", linkId);

        jdbcTemplate.query("select processed_id, type from processed_ids where link_id = :linkId", params, rs -> {
            resultList.add(new ProcessedId(rs.getLong("processed_id"), rs.getString("type")));
        });
        return resultList;
    }

    public void saveAll(Long linkId, List<ProcessedIdDTO> nowProcessedIds) {
        MapSqlParameterSource[] params = new MapSqlParameterSource[nowProcessedIds.size()];
        for (int i = 0; i < nowProcessedIds.size(); i++) {
            params[i] = new MapSqlParameterSource();
            params[i].addValue("linkId", linkId);
            params[i].addValue("processedId", nowProcessedIds.get(i).id());
            params[i].addValue("type", nowProcessedIds.get(i).type().type());
        }

        jdbcTemplate.batchUpdate(
                "insert into processed_ids (link_id, processed_id, type) values (:linkId, :processedId, :type)",
                params);
    }
}

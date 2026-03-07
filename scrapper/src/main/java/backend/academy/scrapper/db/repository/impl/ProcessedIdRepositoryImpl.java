package backend.academy.scrapper.db.repository.impl;

import backend.academy.scrapper.db.model.ProcessedId;
import backend.academy.scrapper.db.query.ProcessedIdQuery;
import backend.academy.scrapper.db.repository.ProcessedIdRepository;
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
public class ProcessedIdRepositoryImpl implements ProcessedIdRepository {

    private static final String LINK_ID = "linkId";
    private static final String TYPE = "type";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Set<ProcessedId> findAll(Long linkId) {
        Set<ProcessedId> resultList = new HashSet<>();
        jdbcTemplate.query(
            ProcessedIdQuery.SELECT_BY_LINK_ID.query(),
            new MapSqlParameterSource()
                .addValue(LINK_ID, linkId),
            rs -> {
                resultList.add(
                    new ProcessedId(rs.getLong("processed_id"), rs.getString(TYPE))
                );
            }
        );
        return resultList;
    }

    public void saveAll(Long linkId, List<ProcessedIdDTO> nowProcessedIds) {
        MapSqlParameterSource[] params = nowProcessedIds.stream()
                .map(item -> new MapSqlParameterSource()
                    .addValue(LINK_ID, linkId)
                    .addValue("processedId", item.id())
                    .addValue(TYPE, item.type().type())
                ).toArray(MapSqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(ProcessedIdQuery.INSERT_BATCH.query(), params);
    }
}

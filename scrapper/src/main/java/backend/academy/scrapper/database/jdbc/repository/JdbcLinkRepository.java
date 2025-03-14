package backend.academy.scrapper.database.jdbc.repository;

import backend.academy.scrapper.database.jdbc.mapper.LinkMapper;
import backend.academy.scrapper.database.model.Link;
import java.net.URI;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import scrapper.bot.connectivity.model.request.AddLinkRequest;

@Repository
@RequiredArgsConstructor
public class JdbcLinkRepository {

    private final Clock clock;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final JdbcLinkRepository jdbcLinkRepository;

    @Transactional(
        propagation = Propagation.MANDATORY,
        rollbackFor = DataAccessException.class
    )
    public Link save(AddLinkRequest linkRequest) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        OffsetDateTime createdTime = OffsetDateTime.now(clock);

        params.addValue("link", linkRequest.link().toString());
        params.addValue("updated_at", createdTime);
        Long linkId = jdbcTemplate.queryForObject(
            "insert into links (link, updated_at) values (:link, :updated_at) returning id",
            params,
            Long.class
        );

        saveLinkTags(linkRequest.tags(), linkId);
        saveLinkFilters(linkRequest.filters(), linkId);

        return new Link(
            linkId,
            linkRequest.link(),
            linkRequest.tags(),
            linkRequest.filters(),
            createdTime
        );
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveLinkTags(Set<String> tags, Long linkId) {
        List<MapSqlParameterSource> tagSources = new ArrayList<>();
        for (String tag : tags) {
            MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
            mapSqlParameterSource.addValue("tag", tag);
            mapSqlParameterSource.addValue("link_id", linkId);
            tagSources.add(mapSqlParameterSource);
        }
        jdbcTemplate.batchUpdate(
            "insert into tags (tag, link_id) values (:tag, :link_id)",
            tagSources.toArray(new MapSqlParameterSource[] {})
        );
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveLinkFilters(Set<String> filters, Long linkId) {
        List<MapSqlParameterSource> filterSources = new ArrayList<>();
        for (String filter : filters) {
            MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
            mapSqlParameterSource.addValue("filter", filter);
            mapSqlParameterSource.addValue("link_id", linkId);
            filterSources.add(mapSqlParameterSource);
        }
        jdbcTemplate.batchUpdate(
            "insert into filters (filter, link_id) values (:filter, :link_id)",
            filterSources.toArray(new MapSqlParameterSource[] {})
        );
    }

    public Long findByLink(String link) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("link", link);

        Optional<Long> linkId = jdbcTemplate.query(
            "select id from links where link = :link",
            params,
            (rs, rowNum) -> rs.getLong("id")
        ).stream().findFirst();

        return linkId.orElse(-1L);
    }

    public Link findById(Long linkId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("link_id", linkId);
        String query = """
            select l.id, l.link, l.updated_at, t.tag, f.filter
            from links l
            left join tags t on t.link_id = l.id
            left join filters f on f.link_id = l.id
            where l.id = :linkId
            """;

        List<Link> links =  jdbcTemplate.queryForObject(
            query,
            params,
            new LinkMapper()
        );

        return Optional.ofNullable(links).orElse(new ArrayList<>()).getFirst();
    }

    public boolean existsLink(String link) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("link", link);

        Integer linkCount = jdbcTemplate.queryForObject(
            "select count(link) from links where link = :link",
            params,
            Integer.class
        );

        return Optional.ofNullable(linkCount).orElse(0) == 1;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Link delete(String link) {
        Long linkId = jdbcLinkRepository.findByLink(link);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("link_id", linkId);

        Link deletedLink = new Link();
        deletedLink.tags(new HashSet<>());
        deletedLink.filters(new HashSet<>());

        jdbcTemplate.query(
            "delete from tags where link_id = :link_id returning tag",
            params,
            rs -> {
                deletedLink.tags().add(rs.getString("tag"));
            }
        );

        jdbcTemplate.query(
            "delete from filters where link_id = :link_id returning filter",
            params,
            rs -> {
                deletedLink.filters().add(rs.getString("filter"));
            }
        );

        jdbcTemplate.query(
            "delete from links where id = :link_id returning id, link, updated_at",
            params,
            rs -> {
                deletedLink.id(rs.getLong("id"));
                deletedLink.url(URI.create(rs.getString("link")));
                deletedLink.createdAt(rs.getObject("updated_at", OffsetDateTime.class));
            }
        );

        return deletedLink;
    }

    public List<Link> findAllLinks(List<Long> linkIds) {
        MapSqlParameterSource sources = new MapSqlParameterSource();
        sources.addValue("link_ids", linkIds);

        String query = """
            select l.id, l.link, l.updated_at, t.tag, f.filter
            from links l
            left join tags t on t.link_id = l.id
            left join filters f on f.link_id = l.id
            where l.id in (:linkIds)
        """;

        return jdbcTemplate.queryForObject(
            query,
            sources,
            new LinkMapper()
        );
    }
}

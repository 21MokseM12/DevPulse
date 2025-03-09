package backend.academy.scrapper.database.model.mapper;

import backend.academy.scrapper.database.model.Link;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.RowMapper;

public class LinkMapper implements RowMapper<List<Link>> {

    @Override
    public List<Link> mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<Long, Link> linkMap = new HashMap<>();
        Long linkId = rs.getLong("id");
        Link link = linkMap.get(linkId);
        if (link == null) {
            link = new Link(
                rs.getLong("id"),
                URI.create(rs.getString("link")),
                new ArrayList<>(),
                new ArrayList<>(),
                rs.getObject("updated_at", OffsetDateTime.class)
            );
            linkMap.put(linkId, link);
        }
        String tag = rs.getString("tag");
        if (tag != null && !link.tags().contains(tag)) {
            link.tags().add(tag);
        }

        String filter = rs.getString("filter");
        if (filter != null && !link.filters().contains(filter)) {
            link.filters().add(filter);
        }

        return new ArrayList<>(linkMap.values());
    }
}

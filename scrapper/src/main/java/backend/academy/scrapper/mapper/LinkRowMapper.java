package backend.academy.scrapper.mapper;

import backend.academy.scrapper.db.model.Link;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class LinkRowMapper implements RowMapper<Link> {

    @Override
    public Link mapRow(ResultSet rs, int rowNum) {
        try {
            Set<String> columns = getColumns(rs);
            return new Link(
                rs.getLong("id"),
                URI.create(rs.getString("link")),
                extractSetFromArray(rs, columns, "tags"),
                extractSetFromArray(rs,columns, "filters"),
                rs.getObject("updated_at", OffsetDateTime.class)
            );
        } catch (SQLException e) {
            throw new RuntimeException("Произошли ошибка маппинга ссылки", e);
        }
    }

    private Set<String> getColumns(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        Set<String> columnNameSet = new HashSet<>();
        for (int i = 1; i <= columnCount; i++) {
            columnNameSet.add(meta.getColumnName(i));
        }
        return columnNameSet;
    }

    private Set<String> extractSetFromArray(ResultSet rs, Set<String> columnNames, String columnName)
        throws SQLException {
        if (!columnNames.contains(columnName.toLowerCase())) {
            return new HashSet<>();
        }

        Array array = rs.getArray(columnName);
        if (array == null || rs.wasNull()) {
            return new HashSet<>();
        }

        String[] stringArray = (String[]) array.getArray();
        return stringArray != null
            ? new HashSet<>(Arrays.asList(stringArray))
            : new HashSet<>();
    }
}

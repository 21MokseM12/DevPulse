package backend.academy.scrapper.db.query;

public enum LinkQuery implements ScrapperQueryHolder {
    INSERT("/db/query/links/insert.sql"),
    SELECT_BY_LINK("/db/query/links/select_by_link.sql"),
    SELECT_BY_ID("/db/query/links/select_by_id.sql"),
    SELECT_COUNT_BY_LINK("/db/query/links/select_count_by_link.sql"),
    DELETE_BY_ID("/db/query/links/delete_by_id.sql"),
    SELECT_BY_LINKS_IDS("/db/query/links/select_by_links_ids.sql"),
    SELECT_BY_UPDATED_AT("/db/query/links/select_by_updated_at.sql"),;

    private final String query;

    LinkQuery(String path) {
        query = readQuery(path);
    }

    @Override
    public String query() {
        return query;
    }
}

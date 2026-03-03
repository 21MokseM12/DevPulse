package backend.academy.scrapper.db.query;

public enum LinkQuery implements ScrapperQueryHolder {
    INSERT("/db/query/links/insert.sql"),
    SELECT_BY_LINK("/db/query/links/select_by_link.sql"),
    SELECT_BY_ID("/db/query/links/select_by_id.sql"),;

    private final String query;

    LinkQuery(String path) {
        query = readQuery(path);
    }

    @Override
    public String query() {
        return query;
    }
}

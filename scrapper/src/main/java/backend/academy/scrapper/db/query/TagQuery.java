package backend.academy.scrapper.db.query;

public enum TagQuery implements ScrapperQueryHolder {

    INSERT("/db/query/tags/insert.sql"),
    SELECT_BY_LINK_ID("/db/query/tags/select_by_link_id.sql"),
    DELETE_BY_LINK_ID("/db/query/tags/delete_by_link_id.sql"),;

    private final String query;

    TagQuery(String path) {
        this.query = readQuery(path);
    }

    @Override
    public String query() {
        return query;
    }
}

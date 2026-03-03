package backend.academy.scrapper.db.query;

public enum FilterQuery implements ScrapperQueryHolder {
    INSERT_BATCH("/db/query/filters/insert_batch.sql"),
    SELECT_BY_LINK_ID("/db/query/filters/select_by_link_id.sql"),
    DELETE_BY_LINK_ID("/db/query/filters/delete_by_link_id.sql"),;

    private final String query;

    FilterQuery(String path) {
        query = readQuery(path);
    }

    @Override
    public String query() {
        return query;
    }
}

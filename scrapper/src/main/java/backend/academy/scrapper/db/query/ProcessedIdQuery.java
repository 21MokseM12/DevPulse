package backend.academy.scrapper.db.query;

public enum ProcessedIdQuery implements ScrapperQueryHolder {

    SELECT_BY_LINK_ID("/db/query/processed_id/select_by_link_id.sql"),
    INSERT_BATCH("/db/query/processed_id/insert_batch.sql"),;

    private final String query;

    ProcessedIdQuery(String path) {
        this.query = readQuery(path);
    }

    @Override
    public String query() {
        return query;
    }
}

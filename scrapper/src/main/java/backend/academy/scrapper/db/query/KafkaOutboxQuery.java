package backend.academy.scrapper.db.query;

public enum KafkaOutboxQuery implements ScrapperQueryHolder {
    INSERT("/db/query/kafka_outbox/insert.sql");

    private final String query;

    KafkaOutboxQuery(String path) {
        this.query = readQuery(path);
    }

    @Override
    public String query() {
        return query;
    }
}

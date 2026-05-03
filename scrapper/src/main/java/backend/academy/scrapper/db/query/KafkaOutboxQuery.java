package backend.academy.scrapper.db.query;

public enum KafkaOutboxQuery implements ScrapperQueryHolder {
    INSERT("/db/query/kafka_outbox/insert.sql"),
    SELECT_PENDING_BATCH("/db/query/kafka_outbox/select_pending_batch.sql"),
    MARK_SENT("/db/query/kafka_outbox/mark_sent.sql"),
    INCREMENT_ATTEMPT_COUNT("/db/query/kafka_outbox/increment_attempt_count.sql"),
    COUNT_PENDING("/db/query/kafka_outbox/count_pending.sql");

    private final String query;

    KafkaOutboxQuery(String path) {
        this.query = readQuery(path);
    }

    @Override
    public String query() {
        return query;
    }
}

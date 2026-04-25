insert into kafka_outbox (topic, payload, created_at, sent, attempt_count)
values (:topic, cast(:payload as jsonb), :createdAt, false, 0);

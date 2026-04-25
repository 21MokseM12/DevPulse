CREATE TABLE IF NOT EXISTS kafka_outbox (
    id BIGSERIAL PRIMARY KEY,
    topic TEXT NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    sent BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMP NULL,
    attempt_count INT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS kafka_outbox_unsent_idx ON kafka_outbox (created_at) WHERE sent = FALSE;

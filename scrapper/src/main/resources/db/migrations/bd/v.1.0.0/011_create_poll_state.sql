CREATE TABLE IF NOT EXISTS poll_state (
    link_id BIGINT PRIMARY KEY REFERENCES links(id) ON DELETE CASCADE,
    next_poll_at TIMESTAMP NOT NULL DEFAULT NOW(),
    retry_count INT NOT NULL DEFAULT 0,
    backoff_until TIMESTAMP NULL,
    last_event_hash TEXT NULL,
    last_success_at TIMESTAMP NULL,
    last_error TEXT NULL
);

CREATE INDEX IF NOT EXISTS poll_state_next_poll_at_idx ON poll_state (next_poll_at);

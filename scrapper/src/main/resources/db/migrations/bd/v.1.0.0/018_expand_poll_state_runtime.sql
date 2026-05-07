ALTER TABLE poll_state
    ADD COLUMN IF NOT EXISTS last_checked_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS last_event_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS fail_count INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS next_retry_at TIMESTAMP;

UPDATE poll_state
SET last_checked_at = COALESCE(last_checked_at, last_success_at, next_poll_at),
    fail_count = COALESCE(fail_count, retry_count, 0),
    next_retry_at = COALESCE(next_retry_at, backoff_until);

CREATE INDEX IF NOT EXISTS poll_state_next_retry_at_idx ON poll_state (next_retry_at);

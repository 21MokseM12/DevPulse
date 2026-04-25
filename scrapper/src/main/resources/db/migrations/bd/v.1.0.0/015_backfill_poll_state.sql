INSERT INTO poll_state (link_id, next_poll_at, retry_count, backoff_until, last_event_hash, last_success_at, last_error)
SELECT
    l.id,
    COALESCE(l.last_checked_at, l.updated_at, NOW()),
    0,
    NULL,
    NULL,
    COALESCE(l.last_checked_at, l.updated_at),
    NULL
FROM links l
ON CONFLICT (link_id) DO NOTHING;

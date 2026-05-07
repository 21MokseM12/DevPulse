insert into poll_state
    (link_id, next_poll_at, retry_count, backoff_until, last_event_hash, last_success_at, last_error, last_checked_at, fail_count, next_retry_at)
values (:link_id, :next_poll_at, 0, null, null, :next_poll_at, null, :next_poll_at, 0, null)
on conflict (link_id) do nothing;

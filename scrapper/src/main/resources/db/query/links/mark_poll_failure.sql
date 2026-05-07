with target as (
    select id
    from links
    where url = :url
),
updated as (
    update poll_state ps
    set retry_count = ps.retry_count + 1,
        fail_count = ps.fail_count + 1,
        backoff_until = :checked_at + (
            interval '1 second' *
            least(:max_backoff_seconds, :base_backoff_seconds * power(2, ps.retry_count))
        ),
        next_retry_at = :checked_at + (
            interval '1 second' *
            least(:max_backoff_seconds, :base_backoff_seconds * power(2, ps.retry_count))
        ),
        last_checked_at = :checked_at,
        last_error = :last_error,
        next_poll_at = :checked_at + (
            interval '1 second' *
            least(:max_backoff_seconds, :base_backoff_seconds * power(2, ps.retry_count))
        )
    from target t
    where ps.link_id = t.id
    returning ps.link_id
)
select count(*)
from updated;

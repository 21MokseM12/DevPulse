with target as (
    select id
    from links
    where url = :url
)
update poll_state ps
set retry_count = 0,
    fail_count = 0,
    backoff_until = null,
    next_retry_at = null,
    last_success_at = :checked_at,
    last_checked_at = :checked_at,
    last_error = null,
    next_poll_at = :next_poll_at
from target t
where ps.link_id = t.id;

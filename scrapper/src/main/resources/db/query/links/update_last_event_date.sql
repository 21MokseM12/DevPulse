with target as (
    select id
    from links
    where url = :url
)
update poll_state ps
set last_event_date = :last_event_date
from target t
where ps.link_id = t.id;

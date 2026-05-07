select ps.last_event_date
from poll_state ps
join links l on l.id = ps.link_id
where l.url = :url;

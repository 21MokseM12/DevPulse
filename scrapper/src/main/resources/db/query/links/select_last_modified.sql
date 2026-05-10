select ps.last_modified_at
from poll_state ps
join links l on l.id = ps.link_id
where l.url = :url;

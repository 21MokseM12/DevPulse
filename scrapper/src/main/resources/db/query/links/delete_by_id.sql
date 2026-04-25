delete
from links
where id = :link_id
returning id, url, last_checked_at, created_at;

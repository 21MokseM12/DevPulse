delete
from links
where id = :link_id
returning id, link, updated_at;

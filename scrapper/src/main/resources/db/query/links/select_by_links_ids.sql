select id, link, updated_at
from links
where id in (:link_ids);

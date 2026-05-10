update poll_state ps
set last_modified_at = :last_modified_at
from links l
where ps.link_id = l.id
  and l.url = :url;

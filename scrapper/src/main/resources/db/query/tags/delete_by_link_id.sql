delete
from tags
where link_id = :link_id
returning tag;

delete
from filters
where link_id = :link_id
returning filter;

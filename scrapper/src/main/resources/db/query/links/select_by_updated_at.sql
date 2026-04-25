select url
from links
where last_checked_at <= :highestTimeLimit
limit :limit
offset :offset

select link
from links
where updated_at <= :highestTimeLimit
limit :limit
offset :offset

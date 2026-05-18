select login
from clients
where id in (:ids)
  and login is not null
  and login <> '';

select id
from clients
where login = :login
  and password_hash = :password;

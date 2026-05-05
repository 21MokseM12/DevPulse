select id, password_hash
from clients
where login = :login;

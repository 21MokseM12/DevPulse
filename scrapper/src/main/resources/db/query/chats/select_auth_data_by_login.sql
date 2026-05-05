select id, password_hash
from chats
where login = :login;

select count(id)
from chats
where login = :login
  and password = :password;

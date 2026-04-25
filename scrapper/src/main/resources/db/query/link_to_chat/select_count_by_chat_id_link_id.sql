select count(*)
from client_links
where client_id = :chatId and link_id = :linkId;

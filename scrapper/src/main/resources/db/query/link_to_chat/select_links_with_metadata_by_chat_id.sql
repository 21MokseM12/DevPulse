select l.id,
       l.url,
       l.created_at,
       cl.tags,
       cl.filters
from client_links cl
         join links l on l.id = cl.link_id
where cl.client_id = :chatId;

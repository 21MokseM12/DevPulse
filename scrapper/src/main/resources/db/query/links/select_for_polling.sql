select l.url
from links l
join poll_state ps on ps.link_id = l.id
where ps.next_poll_at <= :now
  and (ps.next_retry_at is null or ps.next_retry_at <= :now)
order by ps.next_poll_at asc, l.id asc
limit :limit
offset :offset;

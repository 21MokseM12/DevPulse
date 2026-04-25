insert into client_links (client_id, link_id, tags, filters, created_at)
values (:chatId, :linkId, cast(:tags as text[]), cast(:filters as text[]), :createdAt)
on conflict (client_id, link_id) do nothing;

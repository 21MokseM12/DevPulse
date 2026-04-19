insert into links_chats (chat_id, link_id)
values (:chatId, :linkId)
on conflict (chat_id, link_id) do nothing;

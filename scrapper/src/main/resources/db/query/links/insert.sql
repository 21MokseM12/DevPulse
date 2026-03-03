insert into links (link, updated_at)
values (:link, :updated_at)
returning id;

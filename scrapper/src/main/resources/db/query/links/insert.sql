insert into links (link, updated_at, url, link_type, last_checked_at, etag, created_at)
values (:url, :last_checked_at, :url, :link_type, :last_checked_at, :etag, :created_at)
returning id;

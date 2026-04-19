package backend.academy.scrapper.db.query;

public enum LinkToChatQuery implements ScrapperQueryHolder {
    INSERT("/db/query/link_to_chat/insert.sql"),
    SELECT_COUNT_BY_CHAT_ID_AND_LINK_ID("/db/query/link_to_chat/select_count_by_chat_id_link_id.sql"),
    DELETE_BY_CHAT_ID_AND_LINK_ID("/db/query/link_to_chat/delete_by_chat_id_link_id.sql"),
    DELETE_BY_CHAT_ID("/db/query/link_to_chat/delete_by_chat_id.sql"),
    SELECT_LINKS_BY_CHAT_ID("/db/query/link_to_chat/select_links_by_chat_id.sql"),
    SELECT_CHAT_BY_LINK_ID("/db/query/link_to_chat/select_chat_by_link_id.sql");

    private final String query;

    LinkToChatQuery(String path) {
        this.query = readQuery(path);
    }

    @Override
    public String query() {
        return query;
    }
}

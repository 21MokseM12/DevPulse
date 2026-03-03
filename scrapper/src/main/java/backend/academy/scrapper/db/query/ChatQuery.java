package backend.academy.scrapper.db.query;

public enum ChatQuery implements ScrapperQueryHolder {

    SELECT_COUNT_BY_ID("/db/query/chats/select_count_by_id.sql"),
    DELETE_BY_ID("/db/query/chats/delete_by_id.sql"),
    INSERT_CHAT("/db/query/chats/insert_chat.sql"),;

    private final String query;

    ChatQuery(String path) {
        this.query = readQuery(path);
    }

    @Override
    public String query() {
        return query;
    }
}

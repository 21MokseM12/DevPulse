package backend.academy.bot.model.requests;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@EqualsAndHashCode
public class UntrackRequest implements StatefulRequest {

    private final Long chatId;

    private String data;

    @Getter
    private boolean isCallbackQuery;

    public UntrackRequest(Long chatId, String data, boolean isCallbackQuery) {
        this.chatId = chatId;
        this.data = data;
        this.isCallbackQuery = isCallbackQuery;
    }

    @Override
    public Long getChatId() {
        return chatId;
    }

    @Override
    public String getData() {
        return data;
    }
}

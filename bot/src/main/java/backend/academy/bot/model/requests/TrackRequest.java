package backend.academy.bot.model.requests;

import backend.academy.bot.enums.TrackCommandStates;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@EqualsAndHashCode
public class TrackRequest implements StatefulRequest {

    private final Long chatId;

    private String message;

    @Getter
    private TrackCommandStates state;

    public TrackRequest(Long chatId, String message) {
        this.chatId = chatId;
        this.message = message;
    }

    @Override
    public Long getChatId() {
        return chatId;
    }

    @Override
    public String getData() {
        return message;
    }
}

package backend.academy.bot.model.requests;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class StatelessRequest implements Request {

    private Long chatId;

    @Getter
    private String command;

    @Override
    public Long getChatId() {
        return chatId;
    }
}

package backend.academy.bot.enums;

import lombok.Getter;

@Getter
public enum TrackCommandStates {
    LINK("Отправьте ссылку, которую хотите отслеживать", "Вы отправили некорректную ссылку"),

    TAGS("Отправьте теги для ссылки через пробел", "Вы ввели некорректные теги"),

    FILTERS("Отправьте фильтры для ссылки через пробел", "Вы ввели некорректные фильтры");

    private final String successMessage;

    private final String errorMessage;

    TrackCommandStates(String successMessage, String errorMessage) {
        this.successMessage = successMessage;
        this.errorMessage = errorMessage;
    }
}

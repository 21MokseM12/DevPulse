package backend.academy.bot.enums;

public enum Messages {

    ERROR("Что-то пошло не по плану..."),

    INVALID_MESSAGE("Вы отправили некорректное сообщение, попробуйте еще раз"),

    SEND_LINK_MESSAGE_UNTRACK("Выберите ссылку, которую не хотите больше отслеживать\n"),

    EMPTY_LINK_LIST("У вас нет отслеживаемых ссылок"),

    DELETE_SUBSCRIBE_MESSAGE("Ссылка больше не отслеживается"),

    WELCOME_MESSAGE("""
        Приветствую тебя в боте, который за тебя будет следить за всеми твоими подписками!
        Советую ознакомиться со всеми моими возможностями, вызвать команду /help
        """),

    SUCCESS_SUBSCRIBE_LINK("Вы подписались на рассылку уведомлений по введенной ссылке");

    private final String message;

    Messages(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}

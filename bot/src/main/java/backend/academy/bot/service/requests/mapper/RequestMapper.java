package backend.academy.bot.service.requests.mapper;

import backend.academy.bot.model.requests.Request;
import com.pengrad.telegrambot.model.Update;

public interface RequestMapper {
    Request map(Update update);

    boolean canMap(Update update);
}

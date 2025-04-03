package backend.academy.bot.service.requests.mapper;

import backend.academy.bot.model.requests.Request;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RequestMapperFactory {

    List<RequestMapper> mappers;

    @Autowired
    public RequestMapperFactory(List<RequestMapper> mappers) {
        this.mappers = mappers;
    }

    public Optional<Request> map(Update update) {
        return mappers.stream()
                .filter(mapper -> mapper.canMap(update))
                .findFirst()
                .map(mapper -> mapper.map(update));
    }
}

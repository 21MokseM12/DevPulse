package backend.academy.scrapper.controller;

import backend.academy.scrapper.exceptions.ResourceNotFoundException;
import backend.academy.scrapper.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.ScrapperResponse;

@Slf4j
@RestController
@RequestMapping("/tg-chat/{id}")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ScrapperResponse> registerChat(@PathVariable Long id) throws BadRequestException {
        if (id == null || id < 0) {
            throw new BadRequestException("Некорректные параметры запроса");
        }
        log.info(String.valueOf(id));
        chatService.register(id);
        return ResponseEntity.ok().body(new ScrapperResponse("Чат зарегистрирован"));
    }

    @DeleteMapping
    public ResponseEntity<ScrapperResponse> unregisterChat(@PathVariable Long id) throws BadRequestException {
        if (id == null || id < 0) {
            throw new BadRequestException("Некорректные параметры запроса");
        }
        if (!chatService.unregister(id)) {
            throw new ResourceNotFoundException("Чат не существует");
        }
        return ResponseEntity.ok().body(new ScrapperResponse("Чат успешно удален"));
    }
}

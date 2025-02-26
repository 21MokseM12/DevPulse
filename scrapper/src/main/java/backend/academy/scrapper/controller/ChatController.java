package backend.academy.scrapper.controller;

import backend.academy.scrapper.exceptions.ResourceNotFoundException;
import backend.academy.scrapper.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scrapper.bot.connectivity.exceptions.BadRequestException;

@RestController
@RequestMapping("/tg-chat/{id}")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<?> registerChat(@PathVariable Long id) throws BadRequestException {
        if (id == null || id < 0) {
            throw new BadRequestException("Некорректные параметры запроса");
        }
        chatService.register(id);
        return ResponseEntity.ok().body("Чат зарегистрирован");
    }

    @DeleteMapping
    public ResponseEntity<?> unregisterChat(@PathVariable Long id) throws BadRequestException {
        if (id == null || id < 0) {
            throw new BadRequestException("Некорректные параметры запроса");
        }
        if (!chatService.unregister(id)) {
            throw new ResourceNotFoundException("Чат не существует");
        }
        return ResponseEntity.ok().body("Чат успешно удален");
    }
}

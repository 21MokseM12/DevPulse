package backend.academy.scrapper.controller;

import backend.academy.scrapper.exceptions.ResourceNotFoundException;
import backend.academy.scrapper.service.ChatService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scrapper.bot.connectivity.exceptions.BadRequestException;

@RestController
@RequestMapping(value = "/tg-chat/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@Validated
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<Void> registerChat(@PathVariable @NotNull @Positive Long id) throws BadRequestException {
        log.info("Get request to register chat with id {}", id);
        chatService.register(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unregisterChat(@PathVariable @NotNull @Positive Long id)
            throws BadRequestException, ResourceNotFoundException {
        log.info("Get request to unregister chat with id {}", id);
        if (!chatService.unregister(id)) {
            throw new ResourceNotFoundException("Чат не существует");
        }
        return ResponseEntity.ok().build();
    }
}

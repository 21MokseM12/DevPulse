package backend.academy.scrapper.controller;

import backend.academy.scrapper.database.ChatService;
import backend.academy.scrapper.exceptions.ResourceNotFoundException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
@Validated
public class ChatController {

    private static final Logger LOG = LogManager.getLogger(ChatController.class);

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<Void> registerChat(@PathVariable @NotNull @Positive Long id) throws BadRequestException {
        LOG.info("Get request to register chat with id {}", id);
        chatService.register(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unregisterChat(@PathVariable @NotNull @Positive Long id)
            throws BadRequestException, ResourceNotFoundException {
        LOG.info("Get request to unregister chat with id {}", id);
        if (!chatService.unregister(id)) {
            throw new ResourceNotFoundException("Чат не существует");
        }
        return ResponseEntity.ok().build();
    }
}

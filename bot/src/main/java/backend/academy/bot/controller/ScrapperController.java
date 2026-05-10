package backend.academy.bot.controller;

import backend.academy.bot.config.ApplicationConfig;
import backend.academy.bot.exceptions.InvalidInternalAuthSecretException;
import backend.academy.bot.exceptions.MissingInternalAuthHeaderException;
import backend.academy.bot.service.notifications.LinkUpdateProcessingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.LinkUpdate;

@RestController
@RequestMapping("/updates")
@RequiredArgsConstructor
public class ScrapperController {

    private final LinkUpdateProcessingService linkUpdateProcessingService;
    private final ApplicationConfig applicationConfig;

    @PostMapping
    public ResponseEntity<Void> notifyLinkUpdate(@Valid @RequestBody LinkUpdate update, HttpServletRequest request)
            throws BadRequestException {
        validateInternalSecret(request);
        linkUpdateProcessingService.process(update);
        return ResponseEntity.ok().build();
    }

    private void validateInternalSecret(HttpServletRequest request) {
        String secretHeader = request.getHeader(applicationConfig.internalHeader());
        if (secretHeader == null || secretHeader.isBlank()) {
            throw new MissingInternalAuthHeaderException("Missing internal auth header");
        }
        if (!applicationConfig.sharedSecret().equals(secretHeader)) {
            throw new InvalidInternalAuthSecretException("Invalid internal auth secret");
        }
    }
}

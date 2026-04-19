package backend.academy.bot.controller;

import backend.academy.bot.exceptions.ChatNotFoundException;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.response.ApiErrorResponse;

@RestControllerAdvice
public class GlobalExceptionControllerHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequestException(BadRequestException e) {
        List<String> stacktrace = Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();

        ApiErrorResponse apiErrorResponse =
                new ApiErrorResponse("Bad request", "400", e.getClass().getSimpleName(), e.getMessage(), stacktrace);

        return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleChatNotFoundException(ChatNotFoundException e) {
        List<String> stacktrace = Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                "Resource not found", "404", e.getClass().getSimpleName(), e.getMessage(), stacktrace);

        return new ResponseEntity<>(apiErrorResponse, HttpStatus.NOT_FOUND);
    }
}

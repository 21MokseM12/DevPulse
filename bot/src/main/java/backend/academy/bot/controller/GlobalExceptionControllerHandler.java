package backend.academy.bot.controller;

import backend.academy.bot.exceptions.ChatNotFoundException;
import backend.academy.bot.exceptions.ClientLifecycleSyncException;
import backend.academy.bot.exceptions.InvalidInternalAuthSecretException;
import backend.academy.bot.exceptions.MissingInternalAuthHeaderException;
import backend.academy.bot.exceptions.RateLimitExceededException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.response.ApiErrorResponse;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionControllerHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequestException(BadRequestException e) {
        return buildErrorResponse("Bad request", "400", e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiErrorResponse> handleValidationException(Exception e) {
        return buildErrorResponse("Bad request", "400", e, HttpStatus.BAD_REQUEST);
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

    @ExceptionHandler(ClientLifecycleSyncException.class)
    public ResponseEntity<ApiErrorResponse> handleClientLifecycleSyncException(ClientLifecycleSyncException e) {
        List<String> stacktrace = Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();

        ApiErrorResponse apiErrorResponse =
                new ApiErrorResponse("Bad gateway", "502", e.getClass().getSimpleName(), e.getMessage(), stacktrace);

        return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(MissingInternalAuthHeaderException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingInternalAuthHeaderException(
            MissingInternalAuthHeaderException e) {
        log.warn("Rejected /updates request: missing internal auth header");
        return buildErrorResponse("Unauthorized", "401", e, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidInternalAuthSecretException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidInternalAuthSecretException(
            InvalidInternalAuthSecretException e) {
        log.warn("Rejected /updates request: invalid internal auth secret");
        return buildErrorResponse("Forbidden", "403", e, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimitExceededException(RateLimitExceededException e) {
        return buildErrorResponse("Too many requests", "429", e, HttpStatus.TOO_MANY_REQUESTS);
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(
            String description, String code, RuntimeException exception, HttpStatus status) {
        return buildErrorResponse(description, code, (Exception) exception, status);
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(
            String description, String code, Exception exception, HttpStatus status) {
        List<String> stacktrace = Arrays.stream(exception.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                description, code, exception.getClass().getSimpleName(), exception.getMessage(), stacktrace);

        return new ResponseEntity<>(apiErrorResponse, status);
    }
}

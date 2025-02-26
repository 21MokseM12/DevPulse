package backend.academy.scrapper.controller.handlers;

import backend.academy.scrapper.exceptions.ResourceNotFoundException;
import scrapper.bot.connectivity.model.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Arrays;
import java.util.List;
import scrapper.bot.connectivity.exceptions.BadRequestException;

@RestControllerAdvice
public class GlobalExceptionControllerHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> badRequestException(final BadRequestException e) {
        List<String> stacktrace = Arrays.stream(e.getStackTrace())
            .map(StackTraceElement::toString)
            .toList();

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
            "Bad request",
            "400",
            e.getClass().getSimpleName(),
            e.getMessage(),
            stacktrace
        );

        return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> resourceNotFoundException(final ResourceNotFoundException e) {
        List<String> stacktrace = Arrays.stream(e.getStackTrace())
            .map(StackTraceElement::toString)
            .toList();

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
            "Resource not found",
            "404",
            e.getClass().getSimpleName(),
            e.getMessage(),
            stacktrace
        );

        return new ResponseEntity<>(apiErrorResponse, HttpStatus.NOT_FOUND);
    }
}

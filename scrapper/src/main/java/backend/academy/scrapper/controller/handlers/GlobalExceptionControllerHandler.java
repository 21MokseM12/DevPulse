package backend.academy.scrapper.controller.handlers;

import backend.academy.scrapper.exceptions.InvalidCredentialsException;
import backend.academy.scrapper.exceptions.ResourceNotFoundException;
import backend.academy.scrapper.exceptions.UnauthorizedException;
import jakarta.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.response.ApiErrorResponse;

@RestControllerAdvice
public class GlobalExceptionControllerHandler {

    @ExceptionHandler(
            exception = {
                BadRequestException.class,
                ConstraintViolationException.class,
                InvalidCredentialsException.class,
                MethodArgumentNotValidException.class,
                HttpMessageNotReadableException.class
            })
    public ResponseEntity<ApiErrorResponse> badRequestException(Exception e) {
        List<String> stacktrace = Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();

        ApiErrorResponse apiErrorResponse =
                new ApiErrorResponse("Bad request", "400", e.getClass().getSimpleName(), e.getMessage(), stacktrace);

        return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> resourceNotFoundException(ResourceNotFoundException e) {
        List<String> stacktrace = Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                "Resource not found", "404", e.getClass().getSimpleName(), e.getMessage(), stacktrace);

        return new ResponseEntity<>(apiErrorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> unauthorizedException(UnauthorizedException e) {
        List<String> stacktrace = Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();

        ApiErrorResponse apiErrorResponse =
                new ApiErrorResponse("Unauthorized", "401", e.getClass().getSimpleName(), e.getMessage(), stacktrace);

        return new ResponseEntity<>(apiErrorResponse, HttpStatus.UNAUTHORIZED);
    }
}

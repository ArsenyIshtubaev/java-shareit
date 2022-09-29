package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handle(IllegalArgumentException e) {
        log.error("Unknown state: UNSUPPORTED_STATUS" + "\n" + e.getMessage());
        return new ErrorResponse("Unknown state: UNSUPPORTED_STATUS", e.getMessage());
    }

}

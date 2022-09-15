package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void handle() {
        EmailValidateException exception = new EmailValidateException("message");
        ErrorResponse response = errorHandler.handle(exception);
        assertNotNull(response);
        assertEquals(response.getDescription(), exception.getMessage());
    }

    @Test
    void handleStorageException() {
        StorageException exception = new StorageException("message");
        ErrorResponse response = errorHandler.handleStorageException(exception);
        assertNotNull(response);
        assertEquals(response.getDescription(), exception.getMessage());
    }

    @Test
    void handleItemException() {
        ItemException exception = new ItemException("message");
        ErrorResponse response = errorHandler.handleItemException(exception);
        assertNotNull(response);
        assertEquals(response.getDescription(), exception.getMessage());
    }

    @Test
    void handleBookingException() {
        BookingException exception = new BookingException("message");
        ErrorResponse response = errorHandler.handleBookingException(exception);
        assertNotNull(response);
        assertEquals(response.getError(), exception.getMessage());
    }
}
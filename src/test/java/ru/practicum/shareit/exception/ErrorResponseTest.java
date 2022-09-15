package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorResponseTest {

    private ErrorResponse errorResponse = new ErrorResponse("error", "description");

    @Test
    void getError() {
        assertEquals("error", errorResponse.getError());
    }

    @Test
    void getDescription() {
        assertEquals("description", errorResponse.getDescription());
    }
}
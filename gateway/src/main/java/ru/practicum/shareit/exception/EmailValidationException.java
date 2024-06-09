package ru.practicum.shareit.exception;

public class EmailValidationException extends RuntimeException {
    public EmailValidationException(final String message) {
        super(message);
    }
}

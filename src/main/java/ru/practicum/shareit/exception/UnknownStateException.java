package ru.practicum.shareit.exception;

public class UnknownStateException extends RuntimeException {
    public UnknownStateException(final String message) {
        super(message);
    }
}

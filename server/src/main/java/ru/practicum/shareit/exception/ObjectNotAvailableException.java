package ru.practicum.shareit.exception;

public class ObjectNotAvailableException extends RuntimeException {
    public ObjectNotAvailableException(final String message) {
        super(message);
    }
}

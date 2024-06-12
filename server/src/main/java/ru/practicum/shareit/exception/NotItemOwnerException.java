package ru.practicum.shareit.exception;

public class NotItemOwnerException extends RuntimeException {
    public NotItemOwnerException(final String message) {
        super(message);
    }
}
package ru.practicum.shareit.exception;

public class HeaderNonexistentException extends RuntimeException {
    public HeaderNonexistentException(final String message) {
        super(message);
    }
}

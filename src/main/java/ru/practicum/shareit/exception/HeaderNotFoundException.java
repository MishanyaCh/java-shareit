package ru.practicum.shareit.exception;

public class HeaderNotFoundException extends RuntimeException {
    public HeaderNotFoundException(final String message) {
        super(message);
    }
}

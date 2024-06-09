package ru.practicum.shareit.exception;

public class BookingIsApprovedException extends RuntimeException {
    public BookingIsApprovedException(final String message) {
        super(message);
    }
}

package ru.practicum.shareit.booking.enums;

import ru.practicum.shareit.exception.UnknownStateException;

public enum State {
    ALL,
    CURRENT,
    FUTURE,
    PAST,
    REJECTED,
    WAITING;

    public static State convertToEnum(String state) {
        String text = state.toUpperCase();
        try {
            return State.valueOf(text);
        } catch (IllegalArgumentException exc) {
            String message = String.format("Unknown state: %s", text);
            throw new UnknownStateException(message);
        }
    }
}

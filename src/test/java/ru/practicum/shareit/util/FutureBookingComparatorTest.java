package ru.practicum.shareit.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FutureBookingComparatorTest {
    private final FutureBookingComparator futureBookingComparator = new FutureBookingComparator();
    private Booking currentBooking;

    @BeforeEach
    void setUp() {
        currentBooking = new Booking(1, new Item(), new User(),
                LocalDateTime.now().withNano(0).plusHours(2),
                LocalDateTime.now().withNano(0).plusDays(1), Status.WAITING);
    }

    @Test
    void shouldReturnZeroWhenStartDateOfCurrentBookingIsEqualStartDateNextBooking() {
        Booking nextBooking = new Booking(2, new Item(), new User(),
                LocalDateTime.now().withNano(0).plusHours(2),
                LocalDateTime.now().withNano(0).plusDays(2), Status.WAITING);

        int result = futureBookingComparator.compare(currentBooking, nextBooking);
        assertEquals(0, result);
    }

    @Test
    void shouldReturnOneWhenStartDateOfCurrentBookingIsAfterStartDateNextBooking() {
        Booking nextBooking = new Booking(2, new Item(), new User(),
                LocalDateTime.now().withNano(0).plusHours(1),
                LocalDateTime.now().withNano(0).plusDays(1), Status.WAITING);

        int result = futureBookingComparator.compare(currentBooking, nextBooking);
        assertEquals(1, result);
    }

    @Test
    void shouldReturnMinusOneWhenStartDateOfCurrentBookingIsBeforeStartDateNextBooking() {
        Booking nextBooking = new Booking(2, new Item(), new User(),
                LocalDateTime.now().withNano(0).plusHours(4),
                LocalDateTime.now().withNano(0).plusDays(1), Status.WAITING);

        int result = futureBookingComparator.compare(currentBooking, nextBooking);
        assertEquals(-1, result);
    }

}
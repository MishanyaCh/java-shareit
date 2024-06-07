package ru.practicum.shareit.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PastBookingComparatorTest {
    private final PastBookingComparator pastBookingComparator = new PastBookingComparator();
    private Booking currentBooking;

    @BeforeEach
    void setUp() {
        currentBooking = new Booking(1, new Item(), new User(), LocalDateTime.MIN,
                LocalDateTime.now().withNano(0).minusHours(2), Status.WAITING);
    }

    @Test
    void shouldReturnZeroWhenEndDateOfCurrentBookingIsEqualEndDateNextBooking() {
        Booking nextBooking = new Booking(2, new Item(), new User(), LocalDateTime.MIN,
                LocalDateTime.now().withNano(0).minusHours(2), Status.WAITING);

        int result = pastBookingComparator.compare(currentBooking, nextBooking);
        assertEquals(0, result);
    }

    @Test
    void shouldReturnOneWhenEndDateOfCurrentBookingIsBeforeEndDateNextBooking() {
        Booking nextBooking = new Booking(2, new Item(), new User(), LocalDateTime.MIN,
                LocalDateTime.now().withNano(0).minusHours(1), Status.WAITING);

        int result = pastBookingComparator.compare(currentBooking, nextBooking);
        assertEquals(1, result);
    }

    @Test
    void shouldReturnMinusOneWhenEndDateOfCurrentBookingIsAfterEndDateNextBooking() {
        Booking nextBooking = new Booking(2, new Item(), new User(), LocalDateTime.MIN,
                LocalDateTime.now().withNano(0).minusHours(5), Status.WAITING);

        int result = pastBookingComparator.compare(currentBooking, nextBooking);
        assertEquals(-1, result);
    }
}
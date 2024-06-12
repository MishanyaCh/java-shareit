package ru.practicum.shareit.util;

import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.Comparator;

public class FutureBookingComparator implements Comparator<Booking> {
    @Override
    public int compare(Booking currentBooking, Booking nextBooking) {
        LocalDateTime startDateForCurrentBooking = currentBooking.getStartBookingDate();
        LocalDateTime startDateForNextBooking = nextBooking.getStartBookingDate();

        if (startDateForCurrentBooking.isBefore(startDateForNextBooking)) {
            return -1;
        } else if (startDateForCurrentBooking.isAfter(startDateForNextBooking)) {
            return 1;
        } else {
            return 0;
        }
    }
}

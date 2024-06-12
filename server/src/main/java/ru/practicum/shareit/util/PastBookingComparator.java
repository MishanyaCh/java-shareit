package ru.practicum.shareit.util;

import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.Comparator;

public class PastBookingComparator implements Comparator<Booking> {
    @Override
    public int compare(Booking currentBooking, Booking nextBooking) {
        LocalDateTime endDateForCurrentBooking = currentBooking.getEndBookingDate();
        LocalDateTime endDateForNextBooking = nextBooking.getEndBookingDate();

        if (endDateForCurrentBooking.isBefore(endDateForNextBooking)) {
            return 1;
        } else if (endDateForCurrentBooking.isAfter(endDateForNextBooking)) {
            return -1;
        } else {
            return 0;
        }
    }
}

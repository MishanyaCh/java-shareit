package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingMapper {
    Booking toBooking(BookingCreateRequestDto bookingDto);

    BookingDto toBookingDto(Booking booking);

    List<BookingDto> toBookingDtoList(List<Booking> bookings);
}

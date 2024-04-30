package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingMapper {
    Booking toBooking(BookingCreateRequestDto bookingDto);

    BookingResponseDto toBookingResponseDto(Booking booking);

    List<BookingResponseDto> toBookingDtoList(List<Booking> bookings);
}

package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface BookingMapper {
    Booking toBooking(BookingCreateRequestDto bookingDto, Item item, User user);

    BookingResponseDto toBookingResponseDto(Booking booking);

    List<BookingResponseDto> toBookingDtoList(List<Booking> bookings);
}

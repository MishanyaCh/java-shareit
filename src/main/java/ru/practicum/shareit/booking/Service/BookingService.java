package ru.practicum.shareit.booking.Service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;

import java.util.List;

public interface BookingService {
    BookingDto createNewBooking(int userId, BookingCreateRequestDto bookingDto);

    BookingDto approveOrRejectBooking(int userId, int bookingId, boolean approved);

    BookingDto getBooking(int userId, int bookingId);

    List<BookingDto> getBookingsForBooker(int userId, String state);

    List<BookingDto> getBookingsForItemsOwner(int ownerId, String state);
}

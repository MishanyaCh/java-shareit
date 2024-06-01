package ru.practicum.shareit.booking.Service;

import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto createNewBooking(int userId, BookingCreateRequestDto bookingDto);

    BookingResponseDto approveOrRejectBooking(int userId, int bookingId, boolean approved);

    BookingResponseDto getBooking(int userId, int bookingId);

    List<BookingResponseDto> getBookingsForBooker(int userId, String state, int from, int size);

    List<BookingResponseDto> getBookingsForItemsOwner(int ownerId, String state, int from, int size);
}

package ru.practicum.shareit.booking.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.exception.DateTimeBookingException;
import ru.practicum.shareit.exception.ObjectNotAvailableException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class BookingMapperImpl implements BookingMapper {
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;

    @Autowired
    public BookingMapperImpl(UserMapper userMapperArg, ItemMapper itemMapperArg) {
        userMapper = userMapperArg;
        itemMapper = itemMapperArg;
    }

    @Override
    public BookingResponseDto toBookingResponseDto(Booking booking) {
        int bookingId = booking.getId();
        LocalDateTime startBookingDate = booking.getStartBookingDate();
        LocalDateTime endBookingDate = booking.getEndBookingDate();
        String status = booking.getStatus().toString();

        User booker = booking.getBooker();
        UserDto userDto = userMapper.toUserDto(booker);
        Item item = booking.getItem();
        ItemDto itemDto = itemMapper.toItemDto(item);
        return new BookingResponseDto(bookingId, startBookingDate, endBookingDate, status, userDto, itemDto);
    }

    @Override
    public Booking toBooking(BookingCreateRequestDto bookingDto, Item item, User user) {
        LocalDateTime startBookingDate = bookingDto.getStart();
        LocalDateTime endBookingDate = bookingDto.getEnd();
        Status status = Status.WAITING; // устанавливаем статус бронирования

        validateBookingData(bookingDto, item, user);
        return new Booking(0, item, user, startBookingDate, endBookingDate, status);
    }

    @Override
    public List<BookingResponseDto> toBookingDtoList(List<Booking> bookings) {
        List<BookingResponseDto> dtoList = new ArrayList<>();
        for (Booking booking : bookings) {
            BookingResponseDto bookingDto = toBookingResponseDto(booking);
            dtoList.add(bookingDto);
        }
        return dtoList;
    }

    private void validateBookingData(BookingCreateRequestDto bookingDto, Item item, User user) {
        LocalDateTime startDate = bookingDto.getStart();
        LocalDateTime endDate = bookingDto.getEnd();
        boolean isAvailable = item.getIsAvailable();
        int ownerId = item.getOwnerId();

        if (endDate.isBefore(startDate)) {
            String message = "Дата окончания бронирования не может быть раньше даты начала бронирования!";
            throw new DateTimeBookingException(message);
        }
        if (endDate.isEqual(startDate)) {
            String message = "Даты начала и окончания бронирования не могут совпадать!";
            throw new DateTimeBookingException(message);
        }
        if (!isAvailable) {
            String message = String.format("Вещь с id=%d не доступна для бронирования!", item.getId());
            throw new ObjectNotAvailableException(message);
        }
        if (user.getId() == ownerId) {
            String message = "Владелец вещи не может бронировать вещь у самого себя!";
            throw new BookingException(message);
        }
    }
}

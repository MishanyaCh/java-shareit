package ru.practicum.shareit.booking.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
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
    public BookingResponseDto toBookingDto(Booking booking) {
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
    public Booking toBooking(BookingCreateRequestDto bookingDto) {
        int itemId = bookingDto.getItemId();
        LocalDateTime startBookingDate = bookingDto.getStart();
        LocalDateTime endBookingDate = bookingDto.getEnd();

        Item item = new Item(itemId, null, null, null, 0);
        return new Booking(0, item, null, startBookingDate, endBookingDate, null);
    }

    @Override
    public List<BookingResponseDto> toBookingDtoList(List<Booking> bookings) {
        List<BookingResponseDto> dtoList = new ArrayList<>();
        for (Booking booking : bookings) {
            BookingResponseDto bookingDto = toBookingDto(booking);
            dtoList.add(bookingDto);
        }
        return dtoList;
    }
}

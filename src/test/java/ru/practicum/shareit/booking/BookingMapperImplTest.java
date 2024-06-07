package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingMapperImpl;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class BookingMapperImplTest {
    @Mock
    private UserMapper userMapper;
    @Mock
    private ItemMapper itemMapper;
    @InjectMocks
    private BookingMapperImpl bookingMapper;
    private UserDto userDto;
    private User savedUser;
    private ItemDto itemDto;
    private Item savedItem;
    private BookingCreateRequestDto inputBookingDto;
    private Booking booking;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1, "User", "User@mail.ru");
        savedUser = new User(1, "User", "User@mail.ru");
        itemDto = new ItemDto(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, null);
        savedItem = new Item(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, 2);
        inputBookingDto = new BookingCreateRequestDto(null, LocalDateTime.MIN, LocalDateTime.MAX);
        booking = new Booking(1, null, null, LocalDateTime.MIN, LocalDateTime.MAX,
                Status.WAITING);
    }

    @Test
    void toBookingResponseDto_when_thenReturnBookingResponseDto() {
        Mockito.when(userMapper.toUserDto(any())).thenReturn(userDto);
        Mockito.when(itemMapper.toItemDto(any())).thenReturn(itemDto);
        BookingResponseDto expected = new BookingResponseDto(1, LocalDateTime.MIN, LocalDateTime.MAX,
                "WAITING", userDto, itemDto);

        BookingResponseDto result = bookingMapper.toBookingResponseDto(booking);

        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getStartBookingDate(), result.getStartBookingDate());
        assertEquals(expected.getEndBookingDate(), result.getEndBookingDate());
        assertEquals(expected.getStatus(), result.getStatus());
        assertNotNull(result.getBookerDto());
        assertNotNull(result.getItemDto());
    }

    @Test
    void toBooking_whenInvoke_thenReturnBooking() {
        Booking expected = new Booking(
                0, savedItem, savedUser, LocalDateTime.MIN, LocalDateTime.MAX, Status.WAITING);

        Booking result = bookingMapper.toBooking(inputBookingDto, savedItem, savedUser);

        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertNotNull(result.getItem());
        assertNotNull(result.getBooker());
        assertEquals(expected.getStartBookingDate(), result.getStartBookingDate());
        assertEquals(expected.getEndBookingDate(), result.getEndBookingDate());
        assertEquals(expected.getStatus(), result.getStatus());
    }

    @Test
    void toBooking_whenEndDateIsBeforeStartDate_thenThrowDateTimeBookingException() {
        inputBookingDto.setStart(LocalDateTime.now().plusHours(1));
        inputBookingDto.setEnd(LocalDateTime.now());
        String expectedMessage = "Дата окончания бронирования не может быть раньше даты начала бронирования!";

        DateTimeBookingException dateTimeBookingException = assertThrows(DateTimeBookingException.class,
                () -> bookingMapper.toBooking(inputBookingDto, savedItem, savedUser));
        String resultMessage = dateTimeBookingException.getMessage();

        assertEquals(expectedMessage, resultMessage);
    }

    @Test
    void toBooking_whenEndDateIsEqualStartDate_thenThrowDateTimeBookingException() {
        inputBookingDto.setStart(LocalDateTime.now().withNano(0));
        inputBookingDto.setEnd(LocalDateTime.now().withNano(0));
        String expectedMessage = "Даты начала и окончания бронирования не могут совпадать!";

        DateTimeBookingException dateTimeBookingException = assertThrows(DateTimeBookingException.class,
                () -> bookingMapper.toBooking(inputBookingDto, savedItem, savedUser));
        String resultMessage = dateTimeBookingException.getMessage();

        assertEquals(expectedMessage, resultMessage);
    }

    @Test
    void toBooking_whenItemIsNotAvailable_thenThrowObjectNotAvailableException() {
        savedItem.setIsAvailable(false);
        String expectedMessage = String.format("Вещь с id=%d не доступна для бронирования!", savedItem.getId());

        ObjectNotAvailableException objectNotAvailableException = assertThrows(ObjectNotAvailableException.class,
                () -> bookingMapper.toBooking(inputBookingDto, savedItem, savedUser));
        String resultMessage = objectNotAvailableException.getMessage();

        assertEquals(expectedMessage, resultMessage);
    }

    @Test
    void toBooking_whenUserIsItemOwner_thenThrowBookingException() {
        savedItem.setOwnerId(1);
        String expectedMessage = "Владелец вещи не может бронировать вещь у самого себя!";

        BookingException bookingException = assertThrows(BookingException.class,
                () -> bookingMapper.toBooking(inputBookingDto, savedItem, savedUser));
        String resultMessage = bookingException.getMessage();

        assertEquals(expectedMessage, resultMessage);
    }

    @Test
    void toBookingDtoList_whenInvoke_thenReturnBookingResponseDtoList() {
        Mockito.when(userMapper.toUserDto(any())).thenReturn(userDto);
        Mockito.when(itemMapper.toItemDto(any())).thenReturn(itemDto);

        List<BookingResponseDto> result = bookingMapper.toBookingDtoList(List.of(booking));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        Mockito.verify(userMapper, times(List.of(booking).size())).toUserDto(any());
        Mockito.verify(itemMapper, times(List.of(booking).size())).toItemDto(any());
    }
}
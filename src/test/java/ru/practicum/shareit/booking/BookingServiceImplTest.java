package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.exception.BookingIsApprovedException;
import ru.practicum.shareit.exception.NotItemOwnerException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.UnknownStateException;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.storage.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static ru.practicum.shareit.util.Page.getSortedPage;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;
    @Captor
    private ArgumentCaptor<Booking> bookingArgumentCaptor;

    @Test
    void createNewBooking_whenUserAndItemFound_thenSaveBooking() {
        int userId = 1;
        User booker = new User(1, "User", "User@mail.ru");
        Item item = new Item(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, anyInt());
        UserDto userDto = new UserDto(booker.getId(), booker.getName(), booker.getEmail());
        ItemDto itemDto = new ItemDto(item.getId(), item.getName(), item.getDescription(),
                item.getIsAvailable(), item.getOwnerId());
        BookingCreateRequestDto inputBookingDto = new BookingCreateRequestDto(
                1, LocalDateTime.now(), LocalDateTime.now().plusDays(2));
        Booking booking = new Booking();
        BookingResponseDto expectedBookingResponseDto = new BookingResponseDto(1, LocalDateTime.now(),
                LocalDateTime.now().plusDays(2), "WAITING", userDto, itemDto);

        Mockito.when(itemRepository.findById(inputBookingDto.getItemId())).thenReturn(Optional.of(item));
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        Mockito.when(bookingMapper.toBooking(inputBookingDto, item, booker)).thenReturn(booking);
        Mockito.when(bookingRepository.save(booking)).thenReturn(booking);
        Mockito.when(bookingMapper.toBookingResponseDto(any(Booking.class))).thenReturn(expectedBookingResponseDto);

        BookingResponseDto result = bookingService.createNewBooking(userId, inputBookingDto);

        assertEquals(expectedBookingResponseDto, result);
        Mockito.verify(itemRepository).findById(inputBookingDto.getItemId());
        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(bookingMapper).toBooking(inputBookingDto, item, booker);
        Mockito.verify(bookingRepository).save(booking);
        Mockito.verify(bookingMapper).toBookingResponseDto(booking);
    }

    @Test
    void createNewBooking_whenBookingItemNotFound_thenThrowObjectNotFoundExceptionAndNotSaveBooking() {
        int userId = 1;
        BookingCreateRequestDto inputBookingDto = new BookingCreateRequestDto(
                1, LocalDateTime.now(), LocalDateTime.now().plusDays(2));

        Mockito.when(itemRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.createNewBooking(userId, inputBookingDto));
        Mockito.verify(userRepository, never()).findById(userId);
        Mockito.verify(bookingMapper, never())
                .toBooking(any(BookingCreateRequestDto.class), any(Item.class), any(User.class));
        Mockito.verify(bookingRepository, never()).save(any(Booking.class));
        Mockito.verify(bookingMapper, never()).toBookingResponseDto(any(Booking.class));
    }

    @Test
    void createNewBooking_whenUserNotFound_thenThrowObjectNotFoundExceptionAndNotSaveBooking() {
        int userId = 0;
        Item item = new Item(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, anyInt());
        BookingCreateRequestDto inputBookingDto = new BookingCreateRequestDto(
                1, LocalDateTime.now(), LocalDateTime.now().plusDays(2));

        Mockito.when(itemRepository.findById(inputBookingDto.getItemId())).thenReturn(Optional.of(item));
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.createNewBooking(userId, inputBookingDto));
        Mockito.verify(itemRepository).findById(inputBookingDto.getItemId());
        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(bookingMapper, never())
                .toBooking(any(BookingCreateRequestDto.class), any(Item.class), any(User.class));
        Mockito.verify(bookingRepository, never()).save(any(Booking.class));
        Mockito.verify(bookingMapper, never()).toBookingResponseDto(any(Booking.class));
    }

    @Test
    void approveOrRejectBooking_whenItemOwnerApprovedBooking_thenUpdateStatusToApproved() {
        int userId = 1;
        int bookingId = 1;
        boolean isApproved = true;
        User booker = new User(1, "User", "User@mail.ru");
        Item item = new Item(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, 1);
        UserDto userDto = new UserDto(booker.getId(), booker.getName(), booker.getEmail());
        ItemDto itemDto = new ItemDto(item.getId(), item.getName(), item.getDescription(),
                item.getIsAvailable(), item.getOwnerId());

        Booking booking = new Booking(1, item, booker,
                null, null, Status.WAITING);
        Booking updatedBooking = new Booking(1, item, booker, null,
                null, Status.APPROVED);
        BookingResponseDto expectedBookingResponseDto = new BookingResponseDto(1, null,
                null, updatedBooking.getStatus().toString(), userDto, itemDto);

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(bookingRepository.save(any())).thenReturn(updatedBooking);
        Mockito.when(bookingMapper.toBookingResponseDto(updatedBooking)).thenReturn(expectedBookingResponseDto);

        BookingResponseDto result = bookingService.approveOrRejectBooking(userId, bookingId, isApproved);

        Mockito.verify(bookingRepository).save(bookingArgumentCaptor.capture());
        Booking updatedBookingInDb = bookingArgumentCaptor.getValue();
        assertEquals(updatedBooking.getStatus(), updatedBookingInDb.getStatus());
        assertEquals(booking.getId(), updatedBookingInDb.getId());
        assertEquals(booking.getItem().getId(), updatedBookingInDb.getItem().getId());
        assertEquals(booking.getBooker().getId(), updatedBookingInDb.getBooker().getId());

        assertEquals(expectedBookingResponseDto, result);
        Mockito.verify(bookingRepository).findById(bookingId);
        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(bookingMapper).toBookingResponseDto(updatedBooking);
    }

    @Test
    void approveOrRejectBooking_whenItemOwnerRejectedBooking_thenUpdateStatusToRejected() {
        int userId = 1;
        int bookingId = 1;
        boolean isApproved = false;
        User booker = new User(1, "User", "User@mail.ru");
        Item item = new Item(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, 1);
        UserDto userDto = new UserDto(booker.getId(), booker.getName(), booker.getEmail());
        ItemDto itemDto = new ItemDto(item.getId(), item.getName(), item.getDescription(),
                item.getIsAvailable(), item.getOwnerId());

        Booking booking = new Booking(1, item, booker,
                null, null, Status.WAITING);
        Booking updatedBooking = new Booking(1, item, booker, null,
                null, Status.REJECTED);
        BookingResponseDto expectedBookingResponseDto = new BookingResponseDto(1, null,
                null, "REJECTED", userDto, itemDto);

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(bookingRepository.save(any(Booking.class))).thenReturn(updatedBooking);
        Mockito.when(bookingMapper.toBookingResponseDto(updatedBooking)).thenReturn(expectedBookingResponseDto);

        BookingResponseDto result = bookingService.approveOrRejectBooking(userId, bookingId, isApproved);

        Mockito.verify(bookingRepository).save(bookingArgumentCaptor.capture());
        Booking updatedBookingInDb = bookingArgumentCaptor.getValue();
        assertEquals(updatedBooking.getStatus(), updatedBookingInDb.getStatus());
        assertEquals(booking.getId(), updatedBookingInDb.getId());
        assertEquals(booking.getItem().getId(), updatedBookingInDb.getItem().getId());
        assertEquals(booking.getBooker().getId(), updatedBookingInDb.getBooker().getId());

        assertEquals(expectedBookingResponseDto, result);
        Mockito.verify(bookingRepository).findById(bookingId);
        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(bookingMapper).toBookingResponseDto(updatedBooking);
    }

    @Test
    void approveOrRejectBooking_whenBookingNotFound_thenThrowObjectNotFoundException() {
        int userId = 1;
        int bookingId = 1;
        boolean isApproved = true;

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.approveOrRejectBooking(userId, bookingId, isApproved));
        Mockito.verify(bookingRepository).findById(bookingId);
        Mockito.verify(userRepository, never()).existsById(userId);
        Mockito.verify(bookingRepository, never()).save(any(Booking.class));
        Mockito.verify(bookingMapper, never()).toBookingResponseDto(any(Booking.class));
    }

    @Test
    void approveOrRejectBooking_whenUserNotFound_thenThrowObjectNotFoundException() {
        int userId = 1;
        int bookingId = 1;
        boolean isApproved = true;
        User booker = new User(1, "User", "User@mail.ru");
        Item item = new Item(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, 1);
        Booking booking = new Booking(1, item, booker, null,
                null, Status.WAITING);

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.approveOrRejectBooking(userId, bookingId, isApproved));
        Mockito.verify(bookingRepository).findById(bookingId);
        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(bookingRepository, never()).save(any(Booking.class));
        Mockito.verify(bookingMapper, never()).toBookingResponseDto(any(Booking.class));
    }

    @Test
    void approveOrRejectBooking_whenUserNotItemOwner_thenThrowNotItemOwnerException() {
        int userId = 1;
        int bookingId = 1;
        boolean isApproved = true;
        User booker = new User(1, "User", "User@mail.ru");
        Item item = new Item(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, 2);
        Booking booking = new Booking(1, item, booker, null,
                null, Status.WAITING);

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(userId)).thenReturn(true);

        assertThrows(NotItemOwnerException.class,
                () -> bookingService.approveOrRejectBooking(userId, bookingId, isApproved));
        Mockito.verify(bookingRepository).findById(bookingId);
        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(bookingRepository, never()).save(any(Booking.class));
        Mockito.verify(bookingMapper, never()).toBookingResponseDto(any(Booking.class));
    }

    @Test
    void approveOrRejectBooking_whenItemAlreadyBooked_thenThrowBookingIsApprovedException() {
        int userId = 1;
        int bookingId = 1;
        boolean isApproved = true;
        User booker = new User(1, "User", "User@mail.ru");
        Item item = new Item(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, 1);
        Booking booking = new Booking(1, item, booker, null,
                null, Status.APPROVED);

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(userId)).thenReturn(true);

        assertThrows(BookingIsApprovedException.class,
                () -> bookingService.approveOrRejectBooking(userId, bookingId, isApproved));
        Mockito.verify(bookingRepository).findById(bookingId);
        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(bookingRepository, never()).save(any(Booking.class));
        Mockito.verify(bookingMapper, never()).toBookingResponseDto(any(Booking.class));
    }

    @Test
    void getBooking_whenBookingAndUserFound_thenReturnedBookingResponseDto() {
        int userId = 1;
        int bookingId = 1;
        User booker = new User(1, "User", "User@mail.ru");
        Item item = new Item(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, anyInt());
        UserDto userDto = new UserDto(booker.getId(), booker.getName(), booker.getEmail());
        ItemDto itemDto = new ItemDto(item.getId(), item.getName(), item.getDescription(),
                item.getIsAvailable(), item.getOwnerId());
        Booking booking = new Booking(1, item, booker, LocalDateTime.now(),
                LocalDateTime.now().plusDays(2), Status.WAITING);
        BookingResponseDto expectedBookingResponseDto = new BookingResponseDto(1, LocalDateTime.now(),
                LocalDateTime.now().plusDays(2), "WAITING", userDto, itemDto);

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(bookingMapper.toBookingResponseDto(booking)).thenReturn(expectedBookingResponseDto);

        BookingResponseDto result = bookingService.getBooking(userId, bookingId);

        assertEquals(expectedBookingResponseDto, result);
        Mockito.verify(bookingRepository).findById(bookingId);
        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(bookingMapper).toBookingResponseDto(booking);
    }

    @Test
    void getBooking_whenBookingNotFound_thenThrowObjectNotFoundException() {
        int userId = 1;
        int bookingId = 1;

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> bookingService.getBooking(userId, bookingId));
        Mockito.verify(userRepository, never()).existsById(userId);
        Mockito.verify(bookingMapper, never()).toBookingResponseDto(any(Booking.class));
    }

    @Test
    void getBooking_whenUserNotFound_thenThrowObjectNotFoundException() {
        int userId = 0;
        int bookingId = 1;
        User booker = new User(1, "User", "User@mail.ru");
        Item item = new Item(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, anyInt());
        Booking booking = new Booking(1, item, booker, LocalDateTime.now(),
                LocalDateTime.now().plusDays(2), Status.WAITING);

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ObjectNotFoundException.class, () -> bookingService.getBooking(userId, bookingId));

        Mockito.verify(bookingRepository).findById(bookingId);
        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(bookingMapper, never()).toBookingResponseDto(any(Booking.class));
    }

    @Test
    void getBooking_whenUserNotItemOwner_thenThrowBookingException() {
        int userId = 22;
        int bookingId = 1;
        int ownerId = 2;
        User booker = new User(1, "User", "User@mail.ru");
        Item item = new Item(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, anyInt());
        item.setOwnerId(ownerId);
        Booking booking = new Booking(1, item, booker, LocalDateTime.now(),
                LocalDateTime.now().plusDays(2), Status.WAITING);

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(userId)).thenReturn(true);

        assertThrows(BookingException.class, () -> bookingService.getBooking(userId, bookingId));
        Mockito.verify(bookingRepository).findById(bookingId);
        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(bookingMapper, never()).toBookingResponseDto(any(Booking.class));
    }

    @Test
    void getBookingsForBooker_whenUserFoundAndStateIsAll_thenReturnedAnswer() {
        int userId = 1;
        String state = "ALL";
        int from = 0;
        int size = 10;
        Sort sort = Sort.by(Sort.Direction.DESC, "startBookingDate");
        Pageable pageable = getSortedPage(from, size, sort);
        Booking booking = new Booking();
        BookingResponseDto expectedResponseDto = new BookingResponseDto(1, LocalDateTime.now(),
                LocalDateTime.now().plusDays(2), "WAITING", null, null);

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(bookingRepository.findBookingsByBookerId(userId, pageable)).thenReturn(List.of(booking));
        Mockito.when(bookingMapper.toBookingDtoList(List.of(booking))).thenReturn(List.of(expectedResponseDto));

        bookingService.getBookingsForBooker(userId, state, from, size);

        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(bookingRepository).findBookingsByBookerId(userId, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByBookerIdAndStartBookingDateBeforeAndEndBookingDateAfter(
                        userId, null, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByBookerIdAndStartBookingDateAfter(userId, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByBookerIdAndEndBookingDateBefore(userId, null, pageable);
        Mockito.verify(bookingRepository, never()).findBookingsByBookerIdAndStatus(userId, Status.REJECTED, pageable);
        Mockito.verify(bookingRepository, never()).findBookingsByBookerIdAndStatus(userId, Status.WAITING, pageable);
        Mockito.verify(bookingMapper).toBookingDtoList(List.of(booking));
    }

    @Test
    void getBookingsForBooker_whenUserFoundAndStateIsRejected_thenReturnedAnswer() {
        int userId = 1;
        String state = "REJECTED";
        int from = 0;
        int size = 10;
        Sort sort = Sort.by(Sort.Direction.DESC, "startBookingDate");
        Pageable pageable = getSortedPage(from, size, sort);
        Booking booking = new Booking();
        BookingResponseDto expectedResponseDto = new BookingResponseDto(1, LocalDateTime.now(),
                LocalDateTime.now().plusDays(2), "REJECTED", null, null);

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(bookingRepository.findBookingsByBookerIdAndStatus(userId, Status.REJECTED, pageable))
                .thenReturn(List.of(booking));
        Mockito.when(bookingMapper.toBookingDtoList(List.of(booking))).thenReturn(List.of(expectedResponseDto));

        bookingService.getBookingsForBooker(userId, state, from, size);

        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(bookingRepository, never()).findBookingsByBookerId(userId, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByBookerIdAndStartBookingDateBeforeAndEndBookingDateAfter(userId, null, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByBookerIdAndStartBookingDateAfter(userId, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByBookerIdAndEndBookingDateBefore(userId, null, pageable);
        Mockito.verify(bookingRepository).findBookingsByBookerIdAndStatus(userId, Status.REJECTED, pageable);
        Mockito.verify(bookingRepository, never()).findBookingsByBookerIdAndStatus(userId, Status.WAITING, pageable);
        Mockito.verify(bookingMapper).toBookingDtoList(List.of(booking));
    }

    @Test
    void getBookingsForBooker_whenUserFoundAndStateIsWaiting_thenReturnedAnswer() {
        int userId = 1;
        String state = "WAITING";
        int from = 0;
        int size = 10;
        Sort sort = Sort.by(Sort.Direction.DESC, "startBookingDate");
        Pageable pageable = getSortedPage(from, size, sort);
        Booking booking = new Booking();
        BookingResponseDto expectedResponseDto = new BookingResponseDto(1, LocalDateTime.now(),
                LocalDateTime.now().plusDays(2), "WAITING", null, null);

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(bookingRepository.findBookingsByBookerIdAndStatus(userId, Status.WAITING, pageable))
                .thenReturn(List.of(booking));
        Mockito.when(bookingMapper.toBookingDtoList(List.of(booking))).thenReturn(List.of(expectedResponseDto));

        bookingService.getBookingsForBooker(userId, state, from, size);

        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(bookingRepository, never()).findBookingsByBookerId(userId, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByBookerIdAndStartBookingDateBeforeAndEndBookingDateAfter(
                        userId, null, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByBookerIdAndStartBookingDateAfter(userId, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByBookerIdAndEndBookingDateBefore(userId, null, pageable);
        Mockito.verify(bookingRepository, never()).findBookingsByBookerIdAndStatus(userId, Status.REJECTED, pageable);
        Mockito.verify(bookingRepository).findBookingsByBookerIdAndStatus(userId, Status.WAITING, pageable);
        Mockito.verify(bookingMapper).toBookingDtoList(List.of(booking));
    }

    @Test
    void getBookingsForBooker_whenUserNotFound_thenThrowObjectNotFoundException() {
        int userId = 0;
        String state = "WAITING";
        int from = 0;
        int size = 10;
        Sort sort = Sort.by(Sort.Direction.DESC, "startBookingDate");
        Pageable pageable = getSortedPage(from, size, sort);

        Mockito.when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getBookingsForBooker(userId, state, from, size));
        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(bookingRepository, never()).findBookingsByBookerId(userId, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByBookerIdAndStartBookingDateBeforeAndEndBookingDateAfter(
                        userId, null, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByBookerIdAndStartBookingDateAfter(userId, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByBookerIdAndEndBookingDateBefore(userId, null, pageable);
        Mockito.verify(bookingRepository, never()).findBookingsByBookerIdAndStatus(userId, Status.REJECTED, pageable);
        Mockito.verify(bookingRepository, never()).findBookingsByBookerIdAndStatus(userId, Status.WAITING, pageable);
        Mockito.verify(bookingMapper, never()).toBookingDtoList(anyList());
    }

    @Test
    void getBookingsForBooker_whenStateUnknown_thenThrowUnknownStateException() {
        int userId = 1;
        String state = "EAGER";
        int from = 0;
        int size = 10;
        Sort sort = Sort.by(Sort.Direction.DESC, "startBookingDate");
        Pageable pageable = getSortedPage(from, size, sort);

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);

        assertThrows(UnknownStateException.class,
                () -> bookingService.getBookingsForBooker(userId, state, from, size));
        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(bookingRepository, never()).findBookingsByBookerId(userId, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByBookerIdAndStartBookingDateBeforeAndEndBookingDateAfter(
                        userId, null, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByBookerIdAndStartBookingDateAfter(userId, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByBookerIdAndEndBookingDateBefore(userId, null, pageable);
        Mockito.verify(bookingRepository, never()).findBookingsByBookerIdAndStatus(userId, Status.REJECTED, pageable);
        Mockito.verify(bookingRepository, never()).findBookingsByBookerIdAndStatus(userId, Status.WAITING, pageable);
        Mockito.verify(bookingMapper, never()).toBookingDtoList(anyList());
    }
    
    @Test
    void getBookingsForItemsOwner_whenItemOwnerFoundAndStateIsAll_thenReturnedAnswer() {
        int ownerId = 2;
        String state = "ALL";
        int from = 0;
        int size = 10;
        Sort sort = Sort.by(Sort.Direction.DESC, "startBookingDate");
        Pageable pageable = getSortedPage(from, size, sort);
        Item item = new Item(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, 2);
        Booking booking = new Booking();
        BookingResponseDto expectedResponseDto = new BookingResponseDto(1, LocalDateTime.now(),
                LocalDateTime.now().plusDays(2), "WAITING", null, null);

        Mockito.when(userRepository.existsById(ownerId)).thenReturn(true);
        Mockito.when(itemRepository.findItemsByOwnerId(ownerId)).thenReturn(List.of(item));
        Mockito.when(bookingRepository.findBookingsByItemOwnerId(ownerId, pageable)).thenReturn(List.of(booking));
        Mockito.when(bookingMapper.toBookingDtoList(List.of(booking))).thenReturn(List.of(expectedResponseDto));

        bookingService.getBookingsForItemsOwner(ownerId, state, from, size);

        Mockito.verify(userRepository).existsById(ownerId);
        Mockito.verify(itemRepository).findItemsByOwnerId(ownerId);
        Mockito.verify(bookingRepository).findBookingsByItemOwnerId(ownerId, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByItemOwnerIdAndStartBookingDateBeforeAndEndBookingDateAfter(ownerId, null, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByItemOwnerIdAndStartBookingDateAfter(ownerId, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByItemOwnerIdAndEndBookingDateBefore(ownerId, null, pageable);
        Mockito.verify(bookingRepository, never()).findBookingsByItemOwnerIdAndStatus(ownerId, Status.REJECTED, pageable);
        Mockito.verify(bookingRepository, never()).findBookingsByItemOwnerIdAndStatus(ownerId, Status.WAITING, pageable);
        Mockito.verify(bookingMapper).toBookingDtoList(List.of(booking));
    }

    @Test
    void getBookingsForItemsOwner_whenItemOwnerFoundAndStateIsRejected_thenReturnedAnswer() {
        int ownerId = 2;
        String state = "REJECTED";
        int from = 0;
        int size = 10;
        Sort sort = Sort.by(Sort.Direction.DESC, "startBookingDate");
        Pageable pageable = getSortedPage(from, size, sort);
        Item item = new Item(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, 2);
        Booking booking = new Booking();
        BookingResponseDto expectedResponseDto = new BookingResponseDto(1, LocalDateTime.now(),
                LocalDateTime.now().plusDays(2), "REJECTED", null, null);

        Mockito.when(userRepository.existsById(ownerId)).thenReturn(true);
        Mockito.when(itemRepository.findItemsByOwnerId(ownerId)).thenReturn(List.of(item));
        Mockito.when(bookingRepository.findBookingsByItemOwnerIdAndStatus(ownerId, Status.REJECTED,pageable))
                .thenReturn(List.of(booking));
        Mockito.when(bookingMapper.toBookingDtoList(List.of(booking))).thenReturn(List.of(expectedResponseDto));

        bookingService.getBookingsForItemsOwner(ownerId, state, from, size);

        Mockito.verify(userRepository).existsById(ownerId);
        Mockito.verify(itemRepository).findItemsByOwnerId(ownerId);
        Mockito.verify(bookingRepository, never()).findBookingsByItemOwnerId(ownerId, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByItemOwnerIdAndStartBookingDateBeforeAndEndBookingDateAfter(ownerId, null, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByItemOwnerIdAndStartBookingDateAfter(ownerId, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByItemOwnerIdAndEndBookingDateBefore(ownerId, null, pageable);
        Mockito.verify(bookingRepository).findBookingsByItemOwnerIdAndStatus(ownerId, Status.REJECTED, pageable);
        Mockito.verify(bookingRepository, never()).findBookingsByItemOwnerIdAndStatus(ownerId, Status.WAITING, pageable);
        Mockito.verify(bookingMapper).toBookingDtoList(List.of(booking));
    }

    @Test
    void getBookingsForItemsOwner_whenItemOwnerFoundAndStateIsWaiting_thenReturnedAnswer() {
        int ownerId = 2;
        String state = "WAITING";
        int from = 0;
        int size = 10;
        Sort sort = Sort.by(Sort.Direction.DESC, "startBookingDate");
        Pageable pageable = getSortedPage(from, size, sort);
        Item item = new Item(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, 2);
        Booking booking = new Booking();
        BookingResponseDto expectedResponseDto = new BookingResponseDto(1, LocalDateTime.now(),
                LocalDateTime.now().plusDays(2), "WAITING", null, null);

        Mockito.when(userRepository.existsById(ownerId)).thenReturn(true);
        Mockito.when(itemRepository.findItemsByOwnerId(ownerId)).thenReturn(List.of(item));
        Mockito.when(bookingRepository.findBookingsByItemOwnerIdAndStatus(ownerId, Status.WAITING,pageable))
                .thenReturn(List.of(booking));
        Mockito.when(bookingMapper.toBookingDtoList(List.of(booking))).thenReturn(List.of(expectedResponseDto));

        bookingService.getBookingsForItemsOwner(ownerId, state, from, size);

        Mockito.verify(userRepository).existsById(ownerId);
        Mockito.verify(itemRepository).findItemsByOwnerId(ownerId);
        Mockito.verify(bookingRepository, never()).findBookingsByItemOwnerId(ownerId, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByItemOwnerIdAndStartBookingDateBeforeAndEndBookingDateAfter(ownerId, null, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByItemOwnerIdAndStartBookingDateAfter(ownerId, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByItemOwnerIdAndEndBookingDateBefore(ownerId, null, pageable);
        Mockito.verify(bookingRepository, never()).findBookingsByItemOwnerIdAndStatus(ownerId, Status.REJECTED, pageable);
        Mockito.verify(bookingRepository).findBookingsByItemOwnerIdAndStatus(ownerId, Status.WAITING, pageable);
        Mockito.verify(bookingMapper).toBookingDtoList(List.of(booking));
    }

    @Test
    void getBookingsForItemsOwner_whenItemOwnerNotFound_thenThrowObjectNotFoundException() {
        int ownerId = 0;
        String state = "WAITING";
        int from = 0;
        int size = 10;
        Sort sort = Sort.by(Sort.Direction.DESC, "startBookingDate");
        Pageable pageable = getSortedPage(from, size, sort);

        Mockito.when(userRepository.existsById(ownerId)).thenReturn(false);

        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getBookingsForItemsOwner(ownerId, state, from, size));
        Mockito.verify(userRepository).existsById(ownerId);
        Mockito.verify(itemRepository, never()).findItemsByOwnerId(ownerId);
        Mockito.verify(bookingRepository, never()).findBookingsByItemOwnerId(ownerId, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByItemOwnerIdAndStartBookingDateBeforeAndEndBookingDateAfter(ownerId, null, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByItemOwnerIdAndStartBookingDateAfter(ownerId, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByItemOwnerIdAndEndBookingDateBefore(ownerId, null, pageable);
        Mockito.verify(bookingRepository, never()).findBookingsByItemOwnerIdAndStatus(ownerId, Status.REJECTED, pageable);
        Mockito.verify(bookingRepository, never()).findBookingsByItemOwnerIdAndStatus(ownerId, Status.WAITING, pageable);
        Mockito.verify(bookingMapper, never()).toBookingDtoList(anyList());
    }

    @Test
    void getBookingsForItemsOwner_whenUserNotItemOwner_thenThrowNotItemOwnerException() {
        int ownerId = 1;
        String state = "WAITING";
        int from = 0;
        int size = 10;
        Sort sort = Sort.by(Sort.Direction.DESC, "startBookingDate");
        Pageable pageable = getSortedPage(from, size, sort);

        Mockito.when(userRepository.existsById(ownerId)).thenReturn(true);
        Mockito.when(itemRepository.findItemsByOwnerId(ownerId)).thenReturn(anyList());

        assertThrows(NotItemOwnerException.class,
                () -> bookingService.getBookingsForItemsOwner(ownerId, state, from, size));
        Mockito.verify(userRepository).existsById(ownerId);
        Mockito.verify(itemRepository).findItemsByOwnerId(ownerId);
        Mockito.verify(bookingRepository, never()).findBookingsByItemOwnerId(ownerId, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByItemOwnerIdAndStartBookingDateBeforeAndEndBookingDateAfter(ownerId, null, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByItemOwnerIdAndStartBookingDateAfter(ownerId, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByItemOwnerIdAndEndBookingDateBefore(ownerId, null, pageable);
        Mockito.verify(bookingRepository, never()).findBookingsByItemOwnerIdAndStatus(ownerId, Status.REJECTED, pageable);
        Mockito.verify(bookingRepository, never()).findBookingsByItemOwnerIdAndStatus(ownerId, Status.WAITING, pageable);
        Mockito.verify(bookingMapper, never()).toBookingDtoList(anyList());
    }

    @Test
    void getBookingsForItemsOwner_whenStateUnknown_thenThrowUnknownStateException() {
        int ownerId = 2;
        String state = "EAGER";
        int from = 0;
        int size = 10;
        Sort sort = Sort.by(Sort.Direction.DESC, "startBookingDate");
        Pageable pageable = getSortedPage(from, size, sort);
        Item item = new Item(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, 2);

        Mockito.when(userRepository.existsById(ownerId)).thenReturn(true);
        Mockito.when(itemRepository.findItemsByOwnerId(ownerId)).thenReturn(List.of(item));

        assertThrows(UnknownStateException.class,
                () -> bookingService.getBookingsForItemsOwner(ownerId, state, from, size));
        Mockito.verify(userRepository).existsById(ownerId);
        Mockito.verify(itemRepository).findItemsByOwnerId(ownerId);
        Mockito.verify(bookingRepository, never()).findBookingsByItemOwnerId(ownerId, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByItemOwnerIdAndStartBookingDateBeforeAndEndBookingDateAfter(ownerId, null, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByItemOwnerIdAndStartBookingDateAfter(ownerId, null, pageable);
        Mockito.verify(bookingRepository, never())
                .findBookingsByItemOwnerIdAndEndBookingDateBefore(ownerId, null, pageable);
        Mockito.verify(bookingRepository, never()).findBookingsByItemOwnerIdAndStatus(ownerId, Status.REJECTED, pageable);
        Mockito.verify(bookingRepository, never()).findBookingsByItemOwnerIdAndStatus(ownerId, Status.WAITING, pageable);
        Mockito.verify(bookingMapper, never()).toBookingDtoList(anyList());
    }
}
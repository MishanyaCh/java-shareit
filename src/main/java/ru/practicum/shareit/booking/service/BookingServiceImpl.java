package ru.practicum.shareit.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.Storage.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.storage.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.practicum.shareit.util.Page.getSortedPage;

@Service
public class BookingServiceImpl implements BookingService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    @Autowired
    public BookingServiceImpl(ItemRepository itemRepositoryArg, UserRepository userRepositoryArg,
                              BookingRepository bookingRepositoryArg, BookingMapper bookingMapperArg) {
        itemRepository = itemRepositoryArg;
        userRepository = userRepositoryArg;
        bookingRepository = bookingRepositoryArg;
        bookingMapper = bookingMapperArg;
    }

    @Override
    public BookingResponseDto createNewBooking(int userId, BookingCreateRequestDto bookingDto) {
        int itemId = bookingDto.getItemId();
        Item savedItem = getItem(itemId); // находим вещь для бронирования в БД
        User savedUser = getUser(userId); // находим бронирующего вещь пользователя в БД
        Booking newBooking = bookingMapper.toBooking(bookingDto, savedItem, savedUser);

        Booking createdBooking = bookingRepository.save(newBooking); // добавляем новую запись в таблицу booking
        return bookingMapper.toBookingResponseDto(createdBooking);
    }

    @Override
    public BookingResponseDto approveOrRejectBooking(int userId, int bookingId, boolean approved) {
        Booking savedBooking = getBooking(bookingId); // находим бронирование в БД
        Item item = savedBooking.getItem();
        int ownerId = item.getOwnerId();
        int itemId = item.getId();
        Status status = savedBooking.getStatus();

        if (!userRepository.existsById(userId)) {
            String message = String.format("Пользователь с id=%d не найден!", userId);
            throw new ObjectNotFoundException(message);
        }
        if (userId != ownerId) { // проверяем является ли пользователь владелельцем вещи
            String message = String.format("Пользователь с id=%d не является владельцем вещи с id=%d", userId, itemId);
            throw new NotItemOwnerException(message);
        }
        if (status == Status.APPROVED) { // проверяем на наличие уже подтвержденного бронирования
            String message = String.format("Бронирование с id=%d уже подтверждено!", bookingId);
            throw new BookingIsApprovedException(message);
        }

        if (approved) {
            savedBooking.setStatus(Status.APPROVED);
        } else {
            savedBooking.setStatus(Status.REJECTED);
        }
        Booking updatedBooking = bookingRepository.save(savedBooking);
        return bookingMapper.toBookingResponseDto(updatedBooking);
    }

    @Override
    public BookingResponseDto getBooking(int userId, int bookingId) {
        Booking savedBooking = getBooking(bookingId);
        int bookerId = savedBooking.getBooker().getId();
        int ownerId = savedBooking.getItem().getOwnerId();

        if (!userRepository.existsById(userId)) {
            String message = String.format("Пользователь с id=%d не найден!", userId);
            throw new ObjectNotFoundException(message);
        }
        if (userId != bookerId) {
            if (userId != ownerId) {
                String message = String.format("У пользователя с id=%d нет права на просмотр бронирования с id=%d",
                        userId, bookingId);
                throw new BookingException(message);
            }
        }
        return bookingMapper.toBookingResponseDto(savedBooking);
    }

    @Override
    public List<BookingResponseDto> getBookingsForBooker(int userId, String state, int from, int size) {
        if (!userRepository.existsById(userId)) {
            String message = String.format("Пользователь с id=%d не найден!", userId);
            throw new ObjectNotFoundException(message);
        }
        State st = State.convertToEnum(state);
        Sort sortedByStartBookingDate = Sort.by(Sort.Direction.DESC, "startBookingDate");
        Pageable pageable = getSortedPage(from, size, sortedByStartBookingDate);
        List<Booking> bookings;

        switch (st) {
            case ALL:
                bookings = bookingRepository.findBookingsByBookerId(userId, pageable);
                break;
            case CURRENT:
                bookings = bookingRepository
                        .findBookingsByBookerIdAndStartBookingDateBeforeAndEndBookingDateAfter(userId,
                                LocalDateTime.now(), LocalDateTime.now(), pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findBookingsByBookerIdAndStartBookingDateAfter(userId,
                        LocalDateTime.now(), pageable);
                break;
            case PAST:
                bookings = bookingRepository.findBookingsByBookerIdAndEndBookingDateBefore(userId,
                        LocalDateTime.now(), pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findBookingsByBookerIdAndStatus(userId, Status.REJECTED, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findBookingsByBookerIdAndStatus(userId, Status.WAITING, pageable);
                break;
            default:
                bookings = new ArrayList<>();
        }
        return bookingMapper.toBookingDtoList(bookings);
    }

    @Override
    public List<BookingResponseDto> getBookingsForItemsOwner(int ownerId, String state, int from, int size) {
        if (!userRepository.existsById(ownerId)) {
            String message = String.format("Пользователь с id=%d не найден!", ownerId);
            throw new ObjectNotFoundException(message);
        }
        List<Item> items = itemRepository.findItemsByOwnerId(ownerId);
        if (items.isEmpty()) {
            String message = String.format("Пользователь с id=%d не является владельцем вещи", ownerId);
            throw new NotItemOwnerException(message);
        }
        State st = State.convertToEnum(state);
        Sort sortedByStartBookingDate = Sort.by(Sort.Direction.DESC, "startBookingDate");
        Pageable pageable = getSortedPage(from, size, sortedByStartBookingDate);
        List<Booking> bookings;

        switch (st) {
            case ALL:
                bookings = bookingRepository.findBookingsByItemOwnerId(ownerId, pageable);
                break;
            case CURRENT:
                bookings = bookingRepository
                        .findBookingsByItemOwnerIdAndStartBookingDateBeforeAndEndBookingDateAfter(
                                ownerId, LocalDateTime.now(), LocalDateTime.now(), pageable);
                break;
            case FUTURE:
                bookings = bookingRepository
                        .findBookingsByItemOwnerIdAndStartBookingDateAfter(ownerId, LocalDateTime.now(), pageable);
                break;
            case PAST:
                bookings = bookingRepository
                        .findBookingsByItemOwnerIdAndEndBookingDateBefore(ownerId, LocalDateTime.now(), pageable);
                break;
            case REJECTED:
                bookings = bookingRepository
                        .findBookingsByItemOwnerIdAndStatus(ownerId, Status.REJECTED, pageable);
                break;
            case WAITING:
                bookings = bookingRepository
                        .findBookingsByItemOwnerIdAndStatus(ownerId, Status.WAITING, pageable);
                break;
            default:
                bookings = new ArrayList<>();
        }
        return bookingMapper.toBookingDtoList(bookings);
    }

    private User getUser(int userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            String message = String.format("Пользователь с id=%d не найден в БД!", userId);
            throw new ObjectNotFoundException(message);
        }
        return optionalUser.get();
    }

    private Item getItem(int itemId) {
        Optional<Item> optionalItem = itemRepository.findById(itemId);
        if (optionalItem.isEmpty()) {
            String message = String.format("Вещь с id=%d не найдена в БД!", itemId);
            throw new ObjectNotFoundException(message);
        }
        return optionalItem.get();
    }

    private Booking getBooking(int bookingId) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            String message = String.format("Бронирование с id=%d не найдено в БД!", bookingId);
            throw new ObjectNotFoundException(message);
        }
        return optionalBooking.get();
    }
}

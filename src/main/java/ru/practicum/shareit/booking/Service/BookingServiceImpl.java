package ru.practicum.shareit.booking.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Storage.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoForCreateBooking;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.Storage.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.Storage.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public BookingDto createNewBooking(int userId, BookingDtoForCreateBooking bookingDto) {
        Booking newBooking = bookingMapper.toBooking(bookingDto);
        int itemId = newBooking.getItem().getId();
        Item savedItem = getItem(itemId); // находим вещь для бронирования в БД
        User savedUser = getUser(userId); // находим бронирующего вещь пользователя в БД

        validateBookingData(newBooking, savedItem, savedUser);
        newBooking.setItem(savedItem); // добавляем данные о бронируемой вещи
        newBooking.setBooker(savedUser); // добавляем данные о бронирующем пользователе
        newBooking.setStatus(Status.WAITING); // устанавливаем статус бронирования

        Booking createdBooking = bookingRepository.save(newBooking); // добавляем новую запись в таблицу booking
        return bookingMapper.toBookingDto(createdBooking);
    }

    @Override
    public BookingDto approveOrRejectBooking(int userId, int bookingId, boolean approved) {
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
        return bookingMapper.toBookingDto(updatedBooking);
    }

    @Override
    public BookingDto getBooking(int userId, int bookingId) {
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
        return bookingMapper.toBookingDto(savedBooking);
    }

    @Override
    public List<BookingDto> getBookingsForBooker(int userId, String state) {
        if (!userRepository.existsById(userId)) {
            String message = String.format("Пользователь с id=%d не найден!", userId);
            throw new ObjectNotFoundException(message);
        }
        State st = State.convertToEnum(state);
        Sort sort = Sort.by(Sort.Direction.DESC, "startBookingDate");
        List<Booking> bookings;

        switch (st) {
            case ALL:
                bookings = bookingRepository.findBookingsByBookerId(userId, sort);
                break;
            case CURRENT:
                bookings = bookingRepository
                        .findBookingsByBookerIdAndStartBookingDateBeforeAndEndBookingDateAfter(userId,
                                LocalDateTime.now(), LocalDateTime.now(), sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findBookingsByBookerIdAndStartBookingDateAfter(userId,
                        LocalDateTime.now(), sort);
                break;
            case PAST:
                bookings = bookingRepository.findBookingsByBookerIdAndEndBookingDateBefore(userId,
                        LocalDateTime.now(), sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findBookingsByBookerIdAndStatus(userId, Status.REJECTED, sort);
                break;
            case WAITING:
                bookings = bookingRepository.findBookingsByBookerIdAndStatus(userId, Status.WAITING, sort);
                break;
            default:
                bookings = new ArrayList<>();
        }
        return bookingMapper.toBookingDtoList(bookings);
    }

    @Override
    public List<BookingDto> getBookingsForItemsOwner(int ownerId, String state) {
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
        List<Booking> bookings;

        switch (st) {
            case ALL:
                bookings = bookingRepository.findBookingsByItemOwner(ownerId);
                break;
            case CURRENT:
                bookings = bookingRepository
                        .findBookingsByItemOwnerAndStartBookingDateAndEndBookingDate(ownerId, LocalDateTime.now());
                break;
            case FUTURE:
                bookings = bookingRepository
                        .findBookingsByItemOwnerAndStartBookingDateAfter(ownerId, LocalDateTime.now());
                break;
            case PAST:
                bookings = bookingRepository
                        .findBookingsByItemOwnerAndEndBookingDateBefore(ownerId, LocalDateTime.now());
                break;
            case REJECTED:
                bookings = bookingRepository.findBookingsByItemOwnerAndStatus(ownerId, Status.REJECTED.toString());
                break;
            case WAITING:
                bookings = bookingRepository.findBookingsByItemOwnerAndStatus(ownerId, Status.WAITING.toString());
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

    private void validateBookingData(Booking booking, Item item, User user) {
        LocalDateTime startDate = booking.getStartBookingDate();
        LocalDateTime endDate = booking.getEndBookingDate();
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

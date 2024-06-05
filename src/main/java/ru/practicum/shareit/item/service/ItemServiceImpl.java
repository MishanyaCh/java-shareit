package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotItemOwnerException;
import ru.practicum.shareit.exception.ObjectNotAvailableException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.Storage.CommentRepository;
import ru.practicum.shareit.item.Storage.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComment;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.storage.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.FutureBookingComparator;
import ru.practicum.shareit.util.PastBookingComparator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.practicum.shareit.util.Page.getPage;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Autowired
    public ItemServiceImpl(UserRepository userRepositoryArg, ItemRepository itemRepositoryArg,
                           ItemMapper itemMapperArg, BookingRepository bookingRepositoryArg,
                           CommentRepository commentRepositoryArg, CommentMapper commentMapperArg) {
        itemRepository = itemRepositoryArg;
        itemMapper = itemMapperArg;
        userRepository = userRepositoryArg;
        bookingRepository = bookingRepositoryArg;
        commentRepository = commentRepositoryArg;
        commentMapper = commentMapperArg;
    }

    @Override
    public ItemDto createItem(int userId, ItemDto itemDto) {
        checkUserExistence(userId);// проверяем наличие пользователя в БД
        Item newItem = itemMapper.toItem(itemDto);
        newItem.setOwnerId(userId);// добавляем id пользователя, т.е привязываем вещь к пользователю
        Item createdItem = itemRepository.save(newItem);// добавляем новую запись в таблицу items
        return itemMapper.toItemDto(createdItem);
    }

    @Override
    public ItemDto updateItem(int userId, int itemId, ItemDto itemDto) {
        Optional<Item> optionalSavedItem = itemRepository.findById(itemId);
        if (optionalSavedItem.isEmpty()) {
            String message = String.format("Вещь с id=%d не найдена!", itemId);
            throw new ObjectNotFoundException(message);
        }
        // получаем сохраненную в таблице items вещь, данные которой нужно обновить
        Item savedItem = optionalSavedItem.get();
        checkUserExistence(userId);// проверяем наличие пользователя в БД
        checkItemOwner(userId, savedItem);// проверяем является ли пользователь владельцем вещи

        Item item = itemMapper.toItem(itemDto);// получаем обновленные данные вещи, которые нужно обновить в БД
        Item updatedItem = updateItemInDb(savedItem, item);// обновляем запись в таблицу items
        return itemMapper.toItemDto(updatedItem);
    }

    @Override
    public List<ItemDtoWithBookingAndComment> getItems(int userId, int from, int size) { // метод для просмотра списка всех вещей владельца
        List<ItemDtoWithBookingAndComment> result = new ArrayList<>();
        List<Item> items = itemRepository.findItemsByOwnerId(userId); // получаем список всех вещей владельца
        // получаем список всех бронирований для всех вещей владельца
        Pageable pageable = getPage(from, size);
        List<Booking> bookings = bookingRepository.findBookingsByItemOwnerId(userId, pageable); // пагинация
        // получаем список всех комментариев для всех вещей владельца
        List<Comment> comments = commentRepository.findCommentsByItemOwner(userId);

        for (Item item : items) {
            int itemId = item.getId();
            // находим последнее завершенное бронирование текущей вещи
            Optional<Booking> optionalLastBooking = findLastPastBooking(bookings, itemId);
            Booking last = optionalLastBooking.orElse(null);
            // находим ближайшее следующее бронироване текущей вещи
            Optional<Booking> optionalNextBooking = findNearestFutureBooking(bookings, itemId);
            Booking next = optionalNextBooking.orElse(null);
            // находим список комментариев для текущей вещи
            List<Comment> commentsForCurrentItem = findCommentsForCurrentItem(comments, itemId);
            List<CommentDto> commentDtoList = commentMapper.toCommentDtoList(commentsForCurrentItem);

            ItemDtoWithBookingAndComment itemDto = itemMapper
                    .toItemDtoWithBookingAndComment(item, last, next, commentDtoList);
            result.add(itemDto);
        }
        return result;
    }

    @Override
    public ItemDtoWithBookingAndComment getItem(int itemId, int userId) { // метод для просмотра информации о определенной вещи любым пользователем
        Optional<Item> optionalItem = itemRepository.findById(itemId); // получаем объект типа Optional
        if (optionalItem.isEmpty()) {
            String message = String.format("Вещь с id=%d не найдена!", itemId);
            throw new ObjectNotFoundException(message);
        }
        Item item = optionalItem.get(); // получаем значение содержащиеся в optionalItem

        // находим список всех бронирований определенной вещи
        List<Booking> bookings = bookingRepository.findBookingsByItemIdAndStatusNot(itemId, Status.REJECTED,
                Sort.by(Sort.Direction.DESC, "startBookingDate"));
        // находим последнее завершенное бронирование вещи
        Optional<Booking> optionalLastBooking = findLastPastBooking(bookings, itemId);
        Booking last = optionalLastBooking.orElse(null);
        // находим ближайшее следующее бронироване вещи
        Optional<Booking> optionalNextBooking = findNearestFutureBooking(bookings, itemId);
        Booking next = optionalNextBooking.orElse(null);
        // находим список комментариев для опреденной вещи
        List<Comment> comments = commentRepository.findCommentsByItemId(itemId);
        List<CommentDto> commentDtoList = commentMapper.toCommentDtoList(comments);

        if (userId != item.getOwnerId()) {
            return itemMapper.toItemDtoWithBookingAndComment(item, null, null, commentDtoList);
        }
        return itemMapper.toItemDtoWithBookingAndComment(item, last, next, commentDtoList);
    }

    @Override
    public List<ItemDto> searchItems(int userId, String text, int from, int size) {
        checkUserExistence(userId);// проверяем наличие пользователя в БД
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        String editText = text.toLowerCase();// приводим текст к нижнему регистру
        Pageable pageable = getPage(from, size);
        List<Item> items = itemRepository.searchItems(editText, pageable); // пагинация
        return itemMapper.toItemDtoList(items);
    }

    @Override
    public CommentDto createComment(int bookerId, int itemId, CommentDto commentDto) {
        Optional<User> optionalUser = userRepository.findById(bookerId); // получаем объект типа Optional
        if (optionalUser.isEmpty()) {
            String message = String.format("Пользователь с id=%d не найден!", bookerId);
            throw new ObjectNotFoundException(message);
        }
        Optional<Item> optionalItem = itemRepository.findById(itemId);
        if (optionalItem.isEmpty()) {
            String message = String.format("Вещь с id=%d не найдена!", itemId);
            throw new ObjectNotFoundException(message);
        }
        checkBookingsExistence(bookerId, itemId); // проверяем, была ли вещь в взята в аренду пользователем

        Item item = optionalItem.get(); // получаем значение содержащиеся в optionalItem
        User user = optionalUser.get(); // получаем значение содержащиеся в optionalUser
        Comment newComment = commentMapper.toComment(commentDto, item, user);

        Comment createdComment = commentRepository.save(newComment); // добавляем новую запись в таблицу comments
        return commentMapper.toCommentDto(createdComment);
    }

    private void checkUserExistence(int userId) {
        boolean isUserExist = userRepository.existsById(userId);
        if (!isUserExist) {
            String message = String.format("Пользователь с id=%d не найден!", userId);
            throw new ObjectNotFoundException(message);
        }
    }

    private void checkItemOwner(int userId, Item item) {
        int itemId = item.getId();
        int ownerId = item.getOwnerId();
        if (ownerId != userId) {
            String message = String.format("Пользователь с id=%d не является владельцем вещи с id=%d! " +
                    "Операция обновления данных невозможна", userId, itemId);
            throw new NotItemOwnerException(message);
        }
    }

    private Item updateItemInDb(Item savedItem, Item updatedDateForItem) {
        String updatedName = updatedDateForItem.getName();
        String updatedDescription = updatedDateForItem.getDescription();
        Boolean isAvailable = updatedDateForItem.getIsAvailable();

        if (updatedName != null) {
            savedItem.setName(updatedName);
        }
        if (updatedDescription != null) {
            savedItem.setDescription(updatedDescription);
        }
        if (isAvailable != null) {
            savedItem.setIsAvailable(isAvailable);
        }
        return itemRepository.save(savedItem);
    }

    private void checkBookingsExistence(int bookerId, int itemId) {
        List<Booking> bookings = bookingRepository
                .findBookingsByItemIdAndBookerIdAndStatus(itemId, bookerId, Status.APPROVED);
        if (bookings.isEmpty()) {
            String message = String.format("Добавление комментария не возможно, так как " +
                    "пользователь с id=%d не брал вещь с id=%d в аренду.", bookerId, itemId);
            throw new ObjectNotAvailableException(message);
        }
        for (Booking booking : bookings) {
            LocalDateTime endBookingDate = booking.getEndBookingDate();
            if (endBookingDate.isBefore(LocalDateTime.now())) {
                break;
            } else {
                String message = String.format("Добавление комментария не возможно, так как вещь с id=%d " +
                        "находится в аренде у пользователя с id=%d", itemId, bookerId);
                throw new ObjectNotAvailableException(message);
            }
        }
    }

    private Optional<Booking> findLastPastBooking(List<Booking> bookings, int itemId) {
        List<Booking> pastBookings = new ArrayList<>();
        if (bookings.isEmpty()) {
            return Optional.empty();
        }

        for (Booking currentBooking : bookings) {
            if (currentBooking.getItem().getId() != itemId) {
                continue;
            }
            LocalDateTime endDateCurrentBooking = currentBooking.getEndBookingDate();
            LocalDateTime startDateCurrentBooking = currentBooking.getStartBookingDate();
            if (endDateCurrentBooking.isAfter(LocalDateTime.now())) {
                if (startDateCurrentBooking.isBefore(LocalDateTime.now())) {
                    pastBookings.add(currentBooking);
                }
                continue;
            }
            pastBookings.add(currentBooking);
        }

        pastBookings.sort(new PastBookingComparator());
        if (pastBookings.isEmpty()) {
            return Optional.empty();
        }
        Booking lastBooking = pastBookings.get(0);
        return Optional.of(lastBooking);
    }

    private Optional<Booking> findNearestFutureBooking(List<Booking> bookings, int itemId) {
        List<Booking> futureBookings = new ArrayList<>();
        if (bookings.isEmpty()) {
            return Optional.empty();
        }

        for (Booking currentBooking : bookings) {
            if (currentBooking.getItem().getId() != itemId) {
                continue;
            }
            LocalDateTime endDateCurrentBooking = currentBooking.getEndBookingDate();
            LocalDateTime startDateCurrentBooking = currentBooking.getStartBookingDate();
            if (endDateCurrentBooking.isAfter(LocalDateTime.now()) &&
                    startDateCurrentBooking.isAfter(LocalDateTime.now())) {
                futureBookings.add(currentBooking);
            }
        }

        futureBookings.sort(new FutureBookingComparator());
        if (futureBookings.isEmpty()) {
            return Optional.empty();
        }
        Booking nextBooking = futureBookings.get(0);
        return Optional.of(nextBooking);
    }

    private List<Comment> findCommentsForCurrentItem(List<Comment> comments, int itemId) {
        List<Comment> result = new ArrayList<>();
        if (comments.isEmpty()) {
            return result;
        }
        for (Comment comment : comments) {
            if (comment.getItem().getId() != itemId) {
                continue;
            }
            result.add(comment);
        }
        return result;
    }
}

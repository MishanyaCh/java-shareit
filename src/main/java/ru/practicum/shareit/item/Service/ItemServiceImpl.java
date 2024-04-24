package ru.practicum.shareit.item.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Storage.BookingRepository;
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
import ru.practicum.shareit.user.Storage.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.FutureBookingComparator;
import ru.practicum.shareit.util.PastBookingComparator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public List<ItemDtoWithBookingAndComment> getItems(int userId) { // метод для просмотра списка всех вещей владельца
        List<ItemDtoWithBookingAndComment> result = new ArrayList<>();
        List<Item> items = itemRepository.findItemsByOwnerId(userId); // получаем список всех вещей владельца
        // получаем список всех бронирований для всех вещей владельца
        List<Booking> bookings = bookingRepository.findBookingsByItemOwner(userId);
        // получаем список всех комментариев для всех вещей владельца
        List<Comment> comments = commentRepository.findCommentsByItemOwner(userId);

        for (Item item : items) {
            int itemId = item.getId();
            List<Booking> lastBookingAndNextBooking = findLastPastBookingAndNearestFutureBooking(bookings, itemId);
            List<Comment> commentsForCurrentItem = findCommentsForCurrentItem(comments, itemId);
            List<CommentDto> commentDtoList = commentMapper.toCommentDtoList(commentsForCurrentItem);

            if (lastBookingAndNextBooking.isEmpty()) {
                ItemDtoWithBookingAndComment itemDto = itemMapper
                        .toItemDtoWithBookingAndComment(item, null, null, commentDtoList);
                result.add(itemDto);
                continue;
            }

            Booking last = lastBookingAndNextBooking.get(0);
            Booking next = lastBookingAndNextBooking.get(1);
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
        // находим последнее завершенное бронирование и ближайшее следующее бронироване вещи
        List<Booking> lastBookingAndNextBooking = findLastPastBookingAndNearestFutureBooking(bookings, itemId);
        // находим список комментариев для опреденной вещи
        List<Comment> comments = commentRepository.findCommentsByItemId(itemId);
        List<CommentDto> commentDtoList = commentMapper.toCommentDtoList(comments);

        if (userId != item.getOwnerId() || lastBookingAndNextBooking.isEmpty()) {
            return itemMapper.toItemDtoWithBookingAndComment(item, null, null, commentDtoList);
        }
        Booking last = lastBookingAndNextBooking.get(0);
        Booking next = lastBookingAndNextBooking.get(1);
        return itemMapper.toItemDtoWithBookingAndComment(item, last, next, commentDtoList);
    }

    @Override
    public List<ItemDto> searchItems(int userId, String text) {
        checkUserExistence(userId);// проверяем наличие пользователя в БД
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        String editText = text.toLowerCase();// приводим текст к нижнему регистру
        List<Item> items = itemRepository.searchItems(editText);
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
        Comment newComment = commentMapper.toComment(commentDto);
        newComment.setItem(item);
        newComment.setAuthor(user);
        newComment.setCreationDate(LocalDateTime.now());
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

    private List<Booking> findLastPastBookingAndNearestFutureBooking(List<Booking> bookings, int itemId) {
        List<Booking> result = new ArrayList<>();
        List<Booking> pastBookings = new ArrayList<>();
        List<Booking> futureBookings = new ArrayList<>();

        if (bookings.isEmpty()) {
            return result;
        }
        for (Booking currentBooking : bookings) {
            if (currentBooking.getItem().getId() != itemId) {
                continue;
            }
            LocalDateTime endDateCurrentBooking = currentBooking.getEndBookingDate();
            LocalDateTime startDateCurrentBooking = currentBooking.getStartBookingDate();
            if (endDateCurrentBooking.isAfter(LocalDateTime.now())) {
                if (startDateCurrentBooking.isAfter(LocalDateTime.now())) {
                    futureBookings.add(currentBooking);
                }
                continue;
            }
            pastBookings.add(currentBooking);
        }

        pastBookings.sort(new PastBookingComparator());
        futureBookings.sort(new FutureBookingComparator());

        if (!pastBookings.isEmpty()) {
            Booking lastBooking = pastBookings.get(0);
            result.add(lastBooking);
        }
        if (!futureBookings.isEmpty()) {
            Booking nearestBooking = futureBookings.get(0);
            result.add(nearestBooking);
        }
        return result;
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

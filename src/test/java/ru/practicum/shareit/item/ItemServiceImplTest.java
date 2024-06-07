package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDtoWithoutItemField;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.NotItemOwnerException;
import ru.practicum.shareit.exception.ObjectNotAvailableException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComment;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentMapper commentMapper;
    @InjectMocks
    private ItemServiceImpl itemService;
    private ItemDto inputItemDto;
    private Item savedItem;
    private User savedUser;
    private Booking booking;
    private CommentDto inputCommentDto;
    private Comment comment;
    private ItemDtoWithBookingAndComment dtoWithBookingAndComment;
    @Captor
    private ArgumentCaptor<Item> itemArgumentCaptor;

    @BeforeEach
    void setUp() {
        inputItemDto = new ItemDto(null, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                false, null);
        savedItem = new Item(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                false, 1);
        savedUser = new User(1, "User", "User@mail.ru");
        booking = new Booking(1, savedItem, savedUser, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), Status.APPROVED);
        comment = new Comment(1, "Отличный нивелир!!", LocalDateTime.now().minusHours(5),
                savedItem, new User());
        inputCommentDto = new CommentDto(null, "Отличный нивелир!!", null, null);

    }

    @Test
    void createItem_whenUserFound_thenSaveItem() {
        int userId = 1;
        ItemDto expectedDto = new ItemDto(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                false, null);
        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(itemMapper.toItem(inputItemDto)).thenReturn(new Item());
        Mockito.when(itemRepository.save(any())).thenReturn(savedItem);
        Mockito.when(itemMapper.toItemDto(any())).thenReturn(expectedDto);

        ItemDto result = itemService.createItem(userId, inputItemDto);

        assertEquals(expectedDto, result);
        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(itemMapper).toItem(inputItemDto);
        Mockito.verify(itemRepository).save(any());
        Mockito.verify(itemMapper).toItemDto(any());
    }

    @Test
    void createItem_whenUserNotFound_thenThrowObjectNotFoundExceptionAndNotSaveItem() {
        int userId = 0;
        Mockito.when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ObjectNotFoundException.class, () -> itemService.createItem(userId, inputItemDto));
        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(itemMapper, never()).toItem(inputItemDto);
        Mockito.verify(itemRepository, never()).save(any());
        Mockito.verify(itemMapper, never()).toItemDto(any());
    }

    @Test
    void updateItem_whenItemFoundAndUserIsItemOwner_thenUpdateItem() {
        int userId = 1;
        int itemId = 1;
        inputItemDto = new ItemDto(null, null, null, true, null);
        Item updatedItem = new Item(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, 1);
        ItemDto excpectedItemDto = new ItemDto(1, "Лазерный нивелир",
                "Лазерный нивелир EX600-Pro", true, null);
        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(itemMapper.toItem(inputItemDto))
                .thenReturn(new Item(0, null, null, true, 0));
        Mockito.when(itemRepository.save(any())).thenReturn(updatedItem);
        Mockito.when(itemMapper.toItemDto(updatedItem)).thenReturn(excpectedItemDto);

        ItemDto result = itemService.updateItem(userId, itemId, inputItemDto);

        assertEquals(excpectedItemDto, result);
        Mockito.verify(itemRepository).findById(itemId);
        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(itemMapper).toItem(inputItemDto);
        Mockito.verify(itemRepository).save(itemArgumentCaptor.capture());
        Item updatedItemInDb = itemArgumentCaptor.getValue();
        assertEquals(savedItem.getName(), updatedItemInDb.getName());
        assertEquals(savedItem.getDescription(), updatedItemInDb.getDescription());
        assertEquals(true, updatedItemInDb.getIsAvailable());
        Mockito.verify(itemMapper).toItemDto(updatedItem);
    }

    @Test
    void updateItem_whenItemNotFound_thenThrowObjectNotFoundExceptionAndNotUpdateItem() {
        int userId = 1;
        int itemId = 0;
        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> itemService.updateItem(userId, itemId, inputItemDto));
        Mockito.verify(itemRepository).findById(itemId);
        Mockito.verify(userRepository, never()).existsById(userId);
        Mockito.verify(itemMapper, never()).toItem(inputItemDto);
        Mockito.verify(itemRepository, never()).save(any());
        Mockito.verify(itemMapper, never()).toItemDto(any());
    }

    @Test
    void updateItem_whenUserNotFound_thenThrowObjectNotFoundExceptionAndNotUpdateItem() {
        int userId = 0;
        int itemId = 1;
        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        Mockito.when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ObjectNotFoundException.class, () -> itemService.updateItem(userId, itemId, inputItemDto));
        Mockito.verify(itemRepository).findById(itemId);
        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(itemMapper, never()).toItem(inputItemDto);
        Mockito.verify(itemRepository, never()).save(any());
        Mockito.verify(itemMapper, never()).toItemDto(any());
    }

    @Test
    void updateItem_whenUserIsNotItemOwner_thenThrowNotItemOwnerExceptionAndNotUpdateItem() {
        int userId = 4;
        int itemId = 1;
        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        Mockito.when(userRepository.existsById(userId)).thenReturn(true);

        assertThrows(NotItemOwnerException.class, () -> itemService.updateItem(userId, itemId, inputItemDto));
        Mockito.verify(itemRepository).findById(itemId);
        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(itemMapper, never()).toItem(inputItemDto);
        Mockito.verify(itemRepository, never()).save(any());
        Mockito.verify(itemMapper, never()).toItemDto(any());
    }

    @Test
    void getItems_whenInvoke_thenReturnNotEmptyList() {
        int userId = 1;
        int from = 0;
        int size = 10;
        List<Item> items = List.of(savedItem);
        CommentDto commentDto = new CommentDto(comment.getId(), comment.getText(),"User",
                comment.getCreationDate());
        BookingDtoWithoutItemField bookingDtoWithoutItemField = new BookingDtoWithoutItemField(booking.getId(), any(),
                booking.getStartBookingDate(), booking.getEndBookingDate());
        ItemDtoWithBookingAndComment itemDtoWithBookingAndComment = new ItemDtoWithBookingAndComment(
                1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro", true,
                null, bookingDtoWithoutItemField, List.of(commentDto), null);

        Mockito.when(itemRepository.findItemsByOwnerId(userId)).thenReturn(items);
        Mockito.when(bookingRepository.findBookingsByItemOwnerId(anyInt(), any())).thenReturn(List.of(booking));
        Mockito.when(commentRepository.findCommentsByItemOwner(userId)).thenReturn(List.of(comment));
        Mockito.when(commentMapper.toCommentDtoList(anyList())).thenReturn(List.of(commentDto));
        Mockito.when(itemMapper.toItemDtoWithBookingAndComment(any(), any(), any(), anyList()))
                .thenReturn(itemDtoWithBookingAndComment);

        List<ItemDtoWithBookingAndComment> result = itemService.getItems(userId, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        Mockito.verify(itemRepository).findItemsByOwnerId(userId);
        Mockito.verify(bookingRepository).findBookingsByItemOwnerId(anyInt(), any());
        Mockito.verify(commentRepository).findCommentsByItemOwner(userId);
        Mockito.verify(commentMapper, times(items.size())).toCommentDtoList(anyList());
        Mockito.verify(itemMapper, times(items.size())).toItemDtoWithBookingAndComment(any(), any(), any(), anyList());
    }

    @Test
    void getItem_whenItemIsBookedByUserAndHasComment_thenReturnItemWithBookingAndComment() {
        int userId = 1;
        int itemId = 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "startBookingDate");
        CommentDto commentDto = new CommentDto(comment.getId(), comment.getText(),"User",
                comment.getCreationDate());
        BookingDtoWithoutItemField bookingDtoWithoutItemField = new BookingDtoWithoutItemField(booking.getId(), any(),
                booking.getStartBookingDate(), booking.getEndBookingDate());
        ItemDtoWithBookingAndComment expectedDto = new ItemDtoWithBookingAndComment(
                1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, null, bookingDtoWithoutItemField, List.of(commentDto),null);

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        Mockito.when(bookingRepository.findBookingsByItemIdAndStatusNot(itemId, Status.REJECTED, sort))
                .thenReturn(List.of(booking));
        Mockito.when(commentRepository.findCommentsByItemId(itemId)).thenReturn(List.of(comment));
        Mockito.when(commentMapper.toCommentDtoList(anyList())).thenReturn(List.of(commentDto));
        Mockito.when(itemMapper.toItemDtoWithBookingAndComment(any(), any(), any(), anyList())).thenReturn(expectedDto);

        ItemDtoWithBookingAndComment result = itemService.getItem(itemId, userId);

        assertNotNull(result);
        assertEquals(expectedDto.getId(), result.getId());
        assertEquals(expectedDto.getName(), result.getName());
        assertEquals(expectedDto.getDescription(), result.getDescription());
        assertEquals(expectedDto.getIsAvailable(), result.getIsAvailable());
        assertNull(result.getLastBooking());
        assertEquals(expectedDto.getNextBooking(), result.getNextBooking());
        assertFalse(result.getComments().isEmpty());
        assertEquals(expectedDto.getComments().size(), result.getComments().size());
        assertNull(result.getRequestId());
        Mockito.verify(itemRepository).findById(itemId);
        Mockito.verify(bookingRepository).findBookingsByItemIdAndStatusNot(itemId, Status.REJECTED, sort);
        Mockito.verify(commentRepository).findCommentsByItemId(itemId);
        Mockito.verify(commentMapper).toCommentDtoList(anyList());
        Mockito.verify(itemMapper).toItemDtoWithBookingAndComment(any(), any(), any(), anyList());
    }

    @Test
    void getItem_whenItemNotFound_thenThrowObjectNotFoundException() {
        int userId = 1;
        int itemId = 0;
        Sort sort = Sort.by(Sort.Direction.DESC, "startBookingDate");
        Mockito.when(itemRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> itemService.getItem(itemId, userId));
        Mockito.verify(itemRepository).findById(itemId);
        Mockito.verify(bookingRepository, never()).findBookingsByItemIdAndStatusNot(itemId, Status.REJECTED, sort);
        Mockito.verify(commentRepository, never()).findCommentsByItemId(itemId);
        Mockito.verify(commentMapper, never()).toCommentDtoList(anyList());
        Mockito.verify(itemMapper, never()).toItemDtoWithBookingAndComment(any(), any(), any(), anyList());
    }

    @Test
    void getItem_whenUserIsNotItemOwner_thenReturnItemWithoutBooking() {
        int userId = 3;
        int itemId = 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "startBookingDate");
        CommentDto commentDto = new CommentDto(comment.getId(), comment.getText(),"User",
                comment.getCreationDate());
        ItemDtoWithBookingAndComment expectedDto = new ItemDtoWithBookingAndComment(
                1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, null, null, List.of(commentDto), null);

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        Mockito.when(commentRepository.findCommentsByItemId(itemId)).thenReturn(List.of(comment));
        Mockito.when(commentMapper.toCommentDtoList(anyList())).thenReturn(List.of(commentDto));
        Mockito.when(itemMapper.toItemDtoWithBookingAndComment(any(), any(), any(), anyList())).thenReturn(expectedDto);

        ItemDtoWithBookingAndComment result = itemService.getItem(itemId, userId);

        assertNotNull(result);
        assertEquals(expectedDto.getId(), result.getId());
        assertEquals(expectedDto.getName(), result.getName());
        assertEquals(expectedDto.getDescription(), result.getDescription());
        assertEquals(expectedDto.getIsAvailable(), result.getIsAvailable());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertFalse(result.getComments().isEmpty());
        assertEquals(expectedDto.getComments().size(), result.getComments().size());
        assertNull(result.getRequestId());
        Mockito.verify(itemRepository).findById(itemId);
        Mockito.verify(bookingRepository).findBookingsByItemIdAndStatusNot(itemId, Status.REJECTED, sort);
        Mockito.verify(commentRepository).findCommentsByItemId(itemId);
        Mockito.verify(commentMapper).toCommentDtoList(anyList());
        Mockito.verify(itemMapper).toItemDtoWithBookingAndComment(any(), any(), any(), anyList());
    }

    @Test
    void searchItems_whenUserFound_thenReturnFoundedItems() {
        int userId = 1;
        String text = "Нивелир";
        int from = 0;
        int size = 10;
        ItemDto expectedDto = new ItemDto(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, null);
        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(itemRepository.searchItems(anyString(), any())).thenReturn(List.of(savedItem));
        Mockito.when(itemMapper.toItemDtoList(anyList())).thenReturn(List.of(expectedDto));

        List<ItemDto> result = itemService.searchItems(userId, text, from, size);

        assertEquals(List.of(expectedDto), result);
        assertTrue(result.size() <= size);
        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(itemRepository).searchItems(anyString(), any());
        Mockito.verify(itemMapper).toItemDtoList(anyList());
    }

    @Test
    void searchItems_whenUserNotFound_thenThrowObjectNotFoundException() {
        int userId = 0;
        String text = "Нивелир";
        int from = 0;
        int size = 10;
        Mockito.when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ObjectNotFoundException.class, () -> itemService.searchItems(userId, text, from, size));
        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(itemRepository, never()).searchItems(anyString(), any());
        Mockito.verify(itemMapper, never()).toItemDtoList(anyList());
    }

    @Test
    void searchItems_whenTextParamIsEmpty_thenReturnEmptyList() {
        int userId = 0;
        String text = "";
        int from = 0;
        int size = 10;
        Mockito.when(userRepository.existsById(userId)).thenReturn(true);

        List<ItemDto> result = itemService.searchItems(userId, text, from, size);

        assertTrue(result.isEmpty());
        Mockito.verify(itemRepository, never()).searchItems(anyString(), any());
        Mockito.verify(itemMapper, never()).toItemDtoList(anyList());
    }

    @Test
    void createComment_whenBookerAndItemFound_thenSaveComment() {
        int userId = 1;
        int itemId = 1;
        comment.setCreationDate(LocalDateTime.now());
        booking.setStartBookingDate(LocalDateTime.now().minusDays(2));
        booking.setEndBookingDate(LocalDateTime.now().minusHours(5));
        CommentDto expectedDto = new CommentDto(1, "Отличный нивелир!!",
                "User", LocalDateTime.now());
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));
        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        Mockito.when(bookingRepository.findBookingsByItemIdAndBookerIdAndStatus(itemId, userId, Status.APPROVED))
                        .thenReturn(List.of(booking));
        Mockito.when(commentMapper.toComment(inputCommentDto, savedItem, savedUser)).thenReturn(new Comment());
        Mockito.when(commentRepository.save(any())).thenReturn(comment);
        Mockito.when(commentMapper.toCommentDto(any())).thenReturn(expectedDto);

        CommentDto result = itemService.createComment(userId, itemId, inputCommentDto);

        assertEquals(expectedDto, result);
        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(itemRepository).findById(itemId);
        Mockito.verify(bookingRepository).findBookingsByItemIdAndBookerIdAndStatus(itemId, userId, Status.APPROVED);
        Mockito.verify(commentMapper).toComment(inputCommentDto, savedItem, savedUser);
        Mockito.verify(commentRepository).save(any());
        Mockito.verify(commentMapper).toCommentDto(any());
    }

    @Test
    void createComment_whenBookerNotFound_thenThrowObjectNotFoundExceptionAndNotSaveComment() {
        int userId = 0;
        int itemId = 1;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> itemService.createComment(userId, itemId, inputCommentDto));
        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(itemRepository, never()).findById(itemId);
        Mockito.verify(bookingRepository, never())
                .findBookingsByItemIdAndBookerIdAndStatus(itemId, userId, Status.APPROVED);
        Mockito.verify(commentMapper, never()).toComment(inputCommentDto, savedItem, savedUser);
        Mockito.verify(commentRepository, never()).save(any());
        Mockito.verify(commentMapper, never()).toCommentDto(any());
    }

    @Test
    void createComment_whenItemNotFound_thenThrowObjectNotFoundExceptionAndNotSaveComment() {
        int userId = 1;
        int itemId = 0;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));
        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> itemService.createComment(userId, itemId, inputCommentDto));
        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(itemRepository).findById(itemId);
        Mockito.verify(bookingRepository, never())
                .findBookingsByItemIdAndBookerIdAndStatus(itemId, userId, Status.APPROVED);
        Mockito.verify(commentMapper, never()).toComment(inputCommentDto, savedItem, savedUser);
        Mockito.verify(commentRepository, never()).save(any());
        Mockito.verify(commentMapper, never()).toCommentDto(any());
    }

    @Test
    void createComment_whenUserNotBookedItem_thenThrowObjectNotAvailableExceptionNotSaveComment() {
        int userId = 3;
        int itemId = 1;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));
        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        Mockito.when(bookingRepository.findBookingsByItemIdAndBookerIdAndStatus(itemId, userId, Status.APPROVED))
                .thenReturn(anyList());

        assertThrows(ObjectNotAvailableException.class,
                () -> itemService.createComment(userId, itemId, inputCommentDto));
        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(itemRepository).findById(itemId);
        Mockito.verify(bookingRepository)
                .findBookingsByItemIdAndBookerIdAndStatus(itemId, userId, Status.APPROVED);
        Mockito.verify(commentMapper, never()).toComment(inputCommentDto, savedItem, savedUser);
        Mockito.verify(commentRepository, never()).save(any());
        Mockito.verify(commentMapper, never()).toCommentDto(any());
    }

    @Test
    void createComment_whenItemHasBookedYet_thenThrowObjectNotAvailableExceptionNotSaveComment() {
        int userId = 1;
        int itemId = 1;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));
        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        Mockito.when(bookingRepository.findBookingsByItemIdAndBookerIdAndStatus(itemId, userId, Status.APPROVED))
                .thenReturn(List.of(booking));

        assertThrows(ObjectNotAvailableException.class,
                () -> itemService.createComment(userId, itemId, inputCommentDto));
        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(itemRepository).findById(itemId);
        Mockito.verify(bookingRepository)
                .findBookingsByItemIdAndBookerIdAndStatus(itemId, userId, Status.APPROVED);
        Mockito.verify(commentMapper, never()).toComment(inputCommentDto, savedItem, savedUser);
        Mockito.verify(commentRepository, never()).save(any());
        Mockito.verify(commentMapper, never()).toCommentDto(any());
    }
}
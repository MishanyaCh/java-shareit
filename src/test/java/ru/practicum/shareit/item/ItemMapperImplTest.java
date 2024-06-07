package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDtoWithoutItemField;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComment;
import ru.practicum.shareit.item.dto.ItemMapperImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
class ItemMapperImplTest {
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @InjectMocks
    private ItemMapperImpl itemMapper;
    private ItemDto inputItemDto;
    private Item item;
    private ItemRequest itemRequest;
    private Booking lastBooking;
    private BookingDtoWithoutItemField last;
    private Booking nextBooking;
    private BookingDtoWithoutItemField next;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        inputItemDto = new ItemDto(null, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, null);
        item = new Item(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, 2);
        itemRequest = new ItemRequest(1, "Нужен лазерный нивелир", new User(),
                LocalDateTime.now().withNano(0));
        lastBooking = new Booking(1, item, new User(), LocalDateTime.MIN, LocalDateTime.MAX, Status.APPROVED);
        last = new BookingDtoWithoutItemField(1, 3, LocalDateTime.MIN, LocalDateTime.MAX);
        nextBooking = new Booking(2, item, new User(), LocalDateTime.MIN, LocalDateTime.MAX, Status.WAITING);
        next = new BookingDtoWithoutItemField(2, 4, LocalDateTime.MIN, LocalDateTime.MAX);
        commentDto = new CommentDto(1, "Отличный нивелир!!", "User",
                LocalDateTime.now().withNano(0));

    }

    @Test
    void toItemDto_whenItemAddedNotOnRequest_thenReturnItemDto() {
        ItemDto expected = new ItemDto(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, null);

        ItemDto result = itemMapper.toItemDto(item);

        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(expected.getIsAvailable(), result.getIsAvailable());
        assertNull(result.getRequestId());
    }

    @Test
    void toItemDto_whenItemAddedOnRequest_thenReturnItemDto() {
        item.setRequest(itemRequest);
        ItemDto expected = new ItemDto(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, 1);

        ItemDto result = itemMapper.toItemDto(item);

        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(expected.getIsAvailable(), result.getIsAvailable());
        assertNotNull(result.getRequestId());
        assertEquals(expected.getRequestId(), result.getRequestId());
    }

    @Test
    void toItem_whenItemAddedNotOnRequest_thenReturnItem() {
        Item expected = new Item(0, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, 0);

        Item result = itemMapper.toItem(inputItemDto);

        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(expected.getIsAvailable(), result.getIsAvailable());
        assertEquals(expected.getOwnerId(), result.getOwnerId());
        assertNull(expected.getRequest());
        assertNull(result.getRequest());
    }

    @Test
    void toItem_whenItemAddedOnRequest_thenReturnItem() {
        inputItemDto.setRequestId(1);
        Mockito.when(itemRequestRepository.findById(anyInt())).thenReturn(Optional.of(itemRequest));
        Item expected = new Item(0, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, 0);

        Item result = itemMapper.toItem(inputItemDto);

        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(expected.getIsAvailable(), result.getIsAvailable());
        assertEquals(expected.getOwnerId(), result.getOwnerId());
        assertNotNull(result.getRequest());
    }

    @Test
    void toItem_whenItemAddedOnRequestAndRequestNotFound_thenThrowObjectNotFoundException() {
        inputItemDto.setRequestId(1);
        Mockito.when(itemRequestRepository.findById(anyInt())).thenReturn(Optional.empty());
        String exceptedMessage = String.format(
                "Запрос с id=%d на добавление новой вещи не найден!", inputItemDto.getRequestId());

        ObjectNotFoundException objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> itemMapper.toItem(inputItemDto));
        String resultMessage = objectNotFoundException.getMessage();

        assertEquals(exceptedMessage, resultMessage);
    }

    @Test
    void toItemDtoList_whenInvoke_thenReturnItemDtoList() {
        List<ItemDto> result = itemMapper.toItemDtoList(List.of(item));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void toItemDtoWithBookingAndComment_whenLastAndNextBookingsFoundAndHasComment_thenReturnItemDtoWithBookingAndComment() {
        ItemDtoWithBookingAndComment expected = new ItemDtoWithBookingAndComment(
                1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro", true,
                last, next, List.of(commentDto), null);

        ItemDtoWithBookingAndComment result = itemMapper.toItemDtoWithBookingAndComment(
                item, lastBooking, nextBooking, List.of(commentDto));

        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(expected.getIsAvailable(), result.getIsAvailable());
        assertNotNull(result.getLastBooking());
        assertNotNull(result.getNextBooking());
        assertNotNull(result.getComments());
        assertFalse(result.getComments().isEmpty());
        assertEquals(expected.getComments().size(), result.getComments().size());
        assertNull(result.getRequestId());
    }

    @Test
    void toItemDtoWithBookingAndComment_whenLastAndNextBookingsNotFoundAndHasNotComment_thenReturnItemDtoWithoutBookingAndComment() {
        ItemDtoWithBookingAndComment expected = new ItemDtoWithBookingAndComment(
                1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro", true,
                null, null, Collections.emptyList(), null);

        ItemDtoWithBookingAndComment result = itemMapper.toItemDtoWithBookingAndComment(
                item, null, null, Collections.emptyList());

        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(expected.getIsAvailable(), result.getIsAvailable());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertNotNull(result.getComments());
        assertTrue(result.getComments().isEmpty());
        assertNull(result.getRequestId());
    }

    @Test
    void toItemDtoWithBookingAndComment_whenLastFoundAndHasComment_thenReturnItemDtoWithoutBookingAndComment() {
        ItemDtoWithBookingAndComment expected = new ItemDtoWithBookingAndComment(
                1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro", true,
                last, null, List.of(commentDto), null);

        ItemDtoWithBookingAndComment result = itemMapper.toItemDtoWithBookingAndComment(
                item, lastBooking, null, List.of(commentDto));

        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(expected.getIsAvailable(), result.getIsAvailable());
        assertNotNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertNotNull(result.getComments());
        assertFalse(result.getComments().isEmpty());
        assertEquals(expected.getComments().size(), result.getComments().size());
        assertNull(result.getRequestId());
    }

    @Test
    void toItemDtoWithBookingAndComment_whenFoundOnlyNextBooking_thenReturnItemDtoWithoutBookingAndComment() {
        ItemDtoWithBookingAndComment expected = new ItemDtoWithBookingAndComment(
                1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro", true,
                null, next, Collections.emptyList(), null);

        ItemDtoWithBookingAndComment result = itemMapper.toItemDtoWithBookingAndComment(
                item, null, nextBooking, Collections.emptyList());

        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(expected.getIsAvailable(), result.getIsAvailable());
        assertNull(result.getLastBooking());
        assertNotNull(result.getNextBooking());
        assertNotNull(result.getComments());
        assertTrue(result.getComments().isEmpty());
        assertNull(result.getRequestId());
    }
}
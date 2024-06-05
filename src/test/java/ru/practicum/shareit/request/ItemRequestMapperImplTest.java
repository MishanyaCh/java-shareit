package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithAnswers;
import ru.practicum.shareit.request.dto.ItemRequestMapperImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ItemRequestMapperImplTest {
    @Mock
    private ItemMapper itemMapper;
    @InjectMocks
    ItemRequestMapperImpl itemRequestMapper;
    private ItemRequestDto inputItemRequestDto;
    private ItemRequest itemRequest;
    private LocalDateTime creationDate;
    private User requester;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        creationDate = LocalDateTime.now();
        requester = new User(1, "User", "User@mail.ru");
        inputItemRequestDto = new ItemRequestDto(null, "Нужен лазерный нивелир", null);
        itemRequest = new ItemRequest(1, "Нужен лазерный нивелир", requester, creationDate);
        itemDto = new ItemDto(1, "Лазерный Нивелир", "Лазерный нивелир EX-600Pro",
                true, 1);
    }

    @Test
    void toItemRequestDto_whenInvoke_thenReturnItemRequestDto() {
        ItemRequestDto expected = new ItemRequestDto(1, "Нужен лазерный нивелир", creationDate);

        ItemRequestDto result = itemRequestMapper.toItemRequestDto(itemRequest);

        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(expected.getCreated().withNano(0), result.getCreated().withNano(0));
    }

    @Test
    void toItemRequest_whenInvoke_thenReturnItemRequest() {
        ItemRequest expected = new ItemRequest(0, "Нужен лазерный нивелир", requester, creationDate);

        ItemRequest result = itemRequestMapper.toItemRequest(requester, inputItemRequestDto);

        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getDescription(), result.getDescription());
        assertNotNull(result.getRequester());
        assertNotNull(result.getCreationDate());
        assertEquals(expected.getCreationDate().withNano(0), result.getCreationDate().withNano(0));
    }

    @Test
    void toItemRequestDtoWithAnswers_whenInvoke_thenReturnItemRequestWithItemsList() {
        Mockito.when(itemMapper.toItemDtoList(anyList())).thenReturn(List.of(itemDto));
        ItemRequestDtoWithAnswers expected = new ItemRequestDtoWithAnswers(
                1, "Нужен лазерный нивелир", creationDate, List.of(itemDto));

        ItemRequestDtoWithAnswers result = itemRequestMapper
                .toItemRequestDtoWithAnswers(itemRequest, List.of(new Item()));

        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(expected.getCreated(), result.getCreated());
        assertNotNull(result.getItems());
        assertFalse(result.getItems().isEmpty());
        assertEquals(expected.getItems().size(), result.getItems().size());
        Mockito.verify(itemMapper).toItemDtoList(anyList());
    }

    @Test
    void toItemRequestDtoWithAnswers_whenInvoke_thenReturnItemRequestWithEmptyItemList() {
        ItemRequestDtoWithAnswers expected = new ItemRequestDtoWithAnswers(
                1, "Нужен лазерный нивелир", creationDate, new ArrayList<>());

        ItemRequestDtoWithAnswers result = itemRequestMapper
                .toItemRequestDtoWithAnswers(itemRequest, Collections.emptyList());

        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(expected.getCreated(), result.getCreated());
        assertNotNull(result.getItems());
        assertTrue(result.getItems().isEmpty());
        Mockito.verify(itemMapper, never()).toItemDtoList(anyList());
    }
}
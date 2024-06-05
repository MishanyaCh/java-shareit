package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotItemOwnerException;
import ru.practicum.shareit.exception.ObjectNotAvailableException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.Service.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComment;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemService itemService;
    private ItemDto inputItemDto;
    private ItemDto itemResponseDto;
    private CommentDto inputCommentDto;
    private CommentDto commentResponseDto;
    private ItemDtoWithBookingAndComment itemDtoWithBookingAndComment;

    @BeforeEach
    void setUp() {
        inputItemDto = new ItemDto(null, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, null);
        itemResponseDto = new ItemDto(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, null);
        inputCommentDto = new CommentDto(null, "Отличный нивелир!!", null, null);
        commentResponseDto = new CommentDto(1, "Отличный нивелир!!",
                "User", LocalDateTime.now());
        itemDtoWithBookingAndComment = new ItemDtoWithBookingAndComment(1, "Лазерный нивелир",
                "Лазерный нивелир EX600-Pro", true, null, null,
                new ArrayList<>(), null);
    }

    @Test
    @SneakyThrows
    void createItem_whenInvoke_thenStatusCreatedAndHasBodyResponse() {
        String expectedDto = objectMapper.writeValueAsString(itemResponseDto);
        Mockito.when(itemService.createItem(anyInt(), any())).thenReturn(itemResponseDto);

        String result = mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputItemDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertEquals(expectedDto, result);
    }

    @Test
    @SneakyThrows
    void createItem_whenIncorrectItemName_thenStatusBadRequest() {
        inputItemDto.setName("");

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputItemDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemService, never()).createItem(anyInt(), any());
    }

    @Test
    @SneakyThrows
    void createItem_whenItemDescriptionIsNull_thenStatusBadRequest() {
        inputItemDto.setDescription(null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputItemDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemService, never()).createItem(anyInt(), any());
    }

    @Test
    @SneakyThrows
    void createItem_whenItemAvailableIsNull_thenStatusBadRequest() {
        inputItemDto.setIsAvailable(null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputItemDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemService, never()).createItem(anyInt(), any());
    }

    @Test
    @SneakyThrows
    void createItem_whenUserNotFound_thenStatusNotFound() {
        Mockito.when(itemService.createItem(anyInt(), any()))
                .thenThrow(new ObjectNotFoundException(
                        String.format("Пользователь с id=%d не найден. Добавление новой вещи невозможно!", 0)));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputItemDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void updateItem_whenInvoke_thenStatusOKAndHasBodyResponse() {
        inputItemDto = new ItemDto(null, null, null, false, null);
        itemResponseDto.setIsAvailable(false);
        String expectedDto = objectMapper.writeValueAsString(itemResponseDto);
        Mockito.when(itemService.updateItem(anyInt(), anyInt(), any())).thenReturn(itemResponseDto);

        String result = mockMvc.perform(patch("/items/{itemId}", 1)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputItemDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertEquals(expectedDto, result);
    }

    @Test
    @SneakyThrows
    void updateItem_whenItemNotFound_thenStatusNotFound() {
        Mockito.when(itemService.updateItem(anyInt(), anyInt(), any()))
                .thenThrow(new ObjectNotFoundException(String.format("Вещь с id=%d не найдена!", 0)));

        mockMvc.perform(patch("/items/{itemId}", 0)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputItemDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void updateItem_whenUserNotFound_thenStatusNotFound() {
        Mockito.when(itemService.updateItem(anyInt(), anyInt(), any()))
                .thenThrow(new ObjectNotFoundException(String.format("Пользователь с id=%d не найден!", 0)));

        mockMvc.perform(patch("/items/{itemId}", 1)
                        .header("X-Sharer-User-Id", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputItemDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void updateItem_whenUserNotItemOwner_thenStatusNotFound() {
        Mockito.when(itemService.updateItem(anyInt(), anyInt(), any()))
                .thenThrow(new NotItemOwnerException(
                        String.format("Пользователь с id=%d не является владельцем вещи с id=%d! " +
                                "Операция обновляния данных невозможна", 3, 1)));

        mockMvc.perform(patch("/items/{itemId}", 1)
                        .header("X-Sharer-User-Id", 3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputItemDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void getAllOwnerItems_whenIncorrectParamFrom_thenStatusBadRequest() {
        String from = "-1";

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", from)
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void getAllOwnerItems_whenIncorrectParamSize_thenStatusBadRequest() {
        String size = "-1";

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", size))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void getAllOwnerItems_whenInvoke_thenStatusOkAndHasBodyResponse() {
        List<ItemDtoWithBookingAndComment> itemDtoWithBookingAndCommentList = List.of(itemDtoWithBookingAndComment);
        String expectedDto = objectMapper.writeValueAsString(itemDtoWithBookingAndCommentList);
        Mockito.when(itemService.getItems(anyInt(), anyInt(), anyInt())).thenReturn(itemDtoWithBookingAndCommentList);

        String result = mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertEquals(expectedDto, result);
    }

    @Test
    @SneakyThrows
    void getItem_when_thenStatusOkAndHasBodyResponse() {
        String expectedDto = objectMapper.writeValueAsString(itemDtoWithBookingAndComment);
        Mockito.when(itemService.getItem(anyInt(), anyInt())).thenReturn(itemDtoWithBookingAndComment);

        String result = mockMvc.perform(get("/items/{itemId}", 1)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertEquals(expectedDto, result);
    }

    @Test
    @SneakyThrows
    void getItem_whenItemNotFound_thenStatusNotFound() {
        Mockito.when(itemService.getItem(anyInt(), anyInt()))
                .thenThrow(new ObjectNotFoundException(String.format("Вещь с id=%d не найдена!", 0)));

        mockMvc.perform(get("/items/{itemId}", 0)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void searchItemsForUser_whenInvoke_thenStatusOkAndHasBodyResponse() {
        String text = "нивелир";
        List<ItemDto> items = List.of(itemResponseDto);
        String expectedDto = objectMapper.writeValueAsString(items);
        Mockito.when(itemService.searchItems(anyInt(), anyString(), anyInt(), anyInt()))
                .thenReturn(items);

        String result = mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", text)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertEquals(expectedDto, result);
    }

    @Test
    @SneakyThrows
    void searchItemsForUser_whenTextParamIsBlank_thenStatusOkAndHasBodyResponse() {
        String text = "";
        List<ItemDto> items = new ArrayList<>();
        String expectedDto = objectMapper.writeValueAsString(items);
        Mockito.when(itemService.searchItems(anyInt(), anyString(), anyInt(), anyInt()))
                .thenReturn(items);

        String result = mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", text)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertEquals(expectedDto, result);
    }

    @Test
    @SneakyThrows
    void searchItemsForUser_whenIncorrectParamFrom_thenStatusBadRequest() {
        String from = "-1";

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "нивелир")
                        .param("from", from)
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemService, never()).searchItems(anyInt(), anyString(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void searchItemsForUser_whenIncorrectParamsSize_thenStatusBadRequest() {
        String size = "-1";

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "нивелир")
                        .param("from", "0")
                        .param("size", size))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemService, never()).searchItems(anyInt(), anyString(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void searchItemsForUser_whenUserNotFound_thenStatusNotFound() {
        Mockito.when(itemService.searchItems(anyInt(), anyString(), anyInt(), anyInt()))
                .thenThrow(new ObjectNotFoundException(String.format("Пользователь с id=%d не найден!", 0)));

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 0)
                        .param("text", "нивелир")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void createComment_whenInvoke_thenStatusOkAndHasBodyResponse() {
        String expectedDto = objectMapper.writeValueAsString(commentResponseDto);
        Mockito.when(itemService.createComment(anyInt(), anyInt(), any())).thenReturn(commentResponseDto);

        String result = mockMvc.perform(post("/items/{itemId}/comment", 1)
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCommentDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertEquals(expectedDto, result);
    }

    @Test
    @SneakyThrows
    void createComment_whenIncorrectCommentText_thenStatusBadRequest() {
        inputCommentDto.setText("");

        mockMvc.perform(post("/items/{itemId}/comment", 1)
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCommentDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemService, never()).createComment(anyInt(), anyInt(), any());
    }

    @Test
    @SneakyThrows
    void createComment_whenUserNotFound_thenStatusNotFound() {
        Mockito.when(itemService.createComment(anyInt(), anyInt(), any()))
                .thenThrow(new ObjectNotFoundException(String.format("Пользователь с id=%d не найден!", 0)));

        mockMvc.perform(post("/items/{itemId}/comment", 1)
                        .header("X-Sharer-User-Id", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCommentDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void createComment_whenItemNotFound_thenStatusNotFound() {
        Mockito.when(itemService.createComment(anyInt(), anyInt(), any()))
                .thenThrow(new ObjectNotFoundException(String.format("Вещь с id=%d не найдена!", 0)));

        mockMvc.perform(post("/items/{itemId}/comment", 0)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCommentDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void createComment_whenUserNotBookedItem_thenStatusBadRequest() {
        Mockito.when(itemService.createComment(anyInt(), anyInt(), any()))
                .thenThrow(new ObjectNotAvailableException(String.format("Добавление комментария не возможно, так как " +
                        "пользователь с id=%d не брал вещь с id=%d в аренду.", 3, 1)));

        mockMvc.perform(post("/items/{itemId}/comment", 1)
                        .header("X-Sharer-User-Id", 3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCommentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void createComment_whenItemHasBookedYet_thenStatusBadRequest() {
        Mockito.when(itemService.createComment(anyInt(), anyInt(), any()))
                .thenThrow(new ObjectNotAvailableException(String.format("Добавление комментария не возможно, так как " +
                        "вещь с id=%d находится в аренде у пользователя с id=%d", 1, 1)));

        mockMvc.perform(post("/items/{itemId}/comment", 1)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCommentDto)))
                .andExpect(status().isBadRequest());
    }
}
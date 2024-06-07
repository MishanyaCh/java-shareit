package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithAnswers;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ItemRequestControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemRequestService itemRequestService;
    private ItemRequestDto inputItemRequestDto;
    private ItemRequestDto itemRequestResponseDto;
    private ItemRequestDtoWithAnswers requestDtoWithAnswers;

    @BeforeEach
    void setUp() {
        inputItemRequestDto = new ItemRequestDto(null, "Нужен лазерный нивелир", null);
        itemRequestResponseDto = new ItemRequestDto(1, "Нужен лазерный нивелир", LocalDateTime.now());
        requestDtoWithAnswers = new ItemRequestDtoWithAnswers(1, "Нужен лазерный нивелир",
                LocalDateTime.now(), new ArrayList<>());
    }

    @Test
    @SneakyThrows
    void createItemRequest_whenInvoke_thenStatusCreatedAndHasBodyResponse() {
        String expectedDto = objectMapper.writeValueAsString(itemRequestResponseDto);
        Mockito.when(itemRequestService.createItemRequest(anyInt(), any()))
                .thenReturn(itemRequestResponseDto);

        String result = mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputItemRequestDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertEquals(expectedDto, result);
    }

    @Test
    @SneakyThrows
    void createItemRequest_whenUserNotExist_thenStatusNotFound() {
        Mockito.when(itemRequestService.createItemRequest(anyInt(), any()))
                        .thenThrow(new ObjectNotFoundException(
                                String.format("Пользователь c id=%d не найден. Создание запроса невозможно!", 0)));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputItemRequestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void createItemRequest_whenIncorrectDescription_thenStatusBadRequest() {
        inputItemRequestDto.setDescription("");

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputItemRequestDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemRequestService, never()).createItemRequest(anyInt(), any());
    }

    @Test
    @SneakyThrows
    void getRequestsWithAnswersForRequester_whenInvoke_thenStatusOkAndHasBodyResponse() {
        List<ItemRequestDtoWithAnswers> dtoWithAnswers = List.of(requestDtoWithAnswers);
        String expectedDto = objectMapper.writeValueAsString(dtoWithAnswers);
        Mockito.when(itemRequestService.getRequestsWithItemsForRequester(1))
                .thenReturn(dtoWithAnswers);

        String result = mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertEquals(expectedDto, result);
    }

    @Test
    @SneakyThrows
    void getRequestsWithAnswersForRequester_whenUserNotExist_thenStatusNotFound() {
        Mockito.when(itemRequestService.getRequestsWithItemsForRequester(0))
                .thenThrow(new ObjectNotFoundException(String.format("Пользователь c id=%d не найден!", 0)));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 0))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void getOtherRequestsWithAnswers_whenInvoke_thenStatusOkAndHasBodyResponse() {
        List<ItemRequestDtoWithAnswers> dtoWithAnswers = List.of(requestDtoWithAnswers);
        String expectedDto = objectMapper.writeValueAsString(dtoWithAnswers);
        Mockito.when(itemRequestService.getOtherRequestsWithItems(1, 0, 10))
                .thenReturn(dtoWithAnswers);

        String result = mockMvc.perform(get("/requests/all")
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
    void getOtherRequestsWithAnswers_whenUserNotExist_thenStatusNotFound() {
        Mockito.when(itemRequestService.getOtherRequestsWithItems(0, 0, 10))
                .thenThrow(new ObjectNotFoundException(String.format("Пользователь c id=%d не найден!", 0)));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 0)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void getOtherRequestsWithAnswers_whenIncorrectParamFrom_thenStatusBadRequest() {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemRequestService, never()).getOtherRequestsWithItems(1,-1, 10);
    }

    @Test
    @SneakyThrows
    void getOtherRequestsWithAnswers_whenIncorrectParamSize_thenStatusBadRequest() {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "1")
                        .param("size", "-1"))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemRequestService, never()).getOtherRequestsWithItems(1,1, -1);
    }

    @Test
    @SneakyThrows
    void getItemRequest_whenInvoke_thenStatusOkAndHasBodyResponse() {
        String expectedDto = objectMapper.writeValueAsString(requestDtoWithAnswers);
        Mockito.when(itemRequestService.getRequest(1, 1)).thenReturn(requestDtoWithAnswers);

        String result = mockMvc.perform(get("/requests/{requestId}", 1)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertEquals(expectedDto, result);
    }

    @Test
    @SneakyThrows
    void getItemRequest_whenRequestNotFound_thenStatusNotFound() {
        Mockito.when(itemRequestService.getRequest(1, 0))
                .thenThrow(new ObjectNotFoundException(String
                        .format("Запрос c id=%d на добавление новой вещи не найден!", 0)));

        mockMvc.perform(get("/requests/{requestId}", 0)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void getItemRequest_whenUserNotExists_thenStatusNotFound() {
        Mockito.when(itemRequestService.getRequest(0, 1))
                .thenThrow(new ObjectNotFoundException(String.format("Пользователь c id=%d не найден!", 0)));

        mockMvc.perform(get("/requests/{requestId}", 1)
                        .header("X-Sharer-User-Id", 0))
                .andExpect(status().isNotFound());
    }
}

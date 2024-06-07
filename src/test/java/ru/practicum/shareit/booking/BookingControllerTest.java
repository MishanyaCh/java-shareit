package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.UnknownStateException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookingService bookingService;
    private BookingCreateRequestDto inputBookingDto;
    private BookingResponseDto bookingResponseDto;

    @BeforeEach
    void setUp() {
        UserDto userDto = new UserDto(1, "User", "User@mail.ru");
        ItemDto itemDto = new ItemDto(1, "Лазерный нивелир", "Лазерный нивелир EX-Pro600",
                true, null);
        inputBookingDto = new BookingCreateRequestDto(1, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusDays(2));
        bookingResponseDto = new BookingResponseDto(1, inputBookingDto.getStart(), inputBookingDto.getEnd(),
                "WAITING", userDto, itemDto);
    }

    @Test
    @SneakyThrows
    void createBooking_whenInvoke_thenStatusCreatedAndHasBodyResponse() {
        String expectedDto = objectMapper.writeValueAsString(bookingResponseDto);
        Mockito.when(bookingService.createNewBooking(anyInt(), any())).thenReturn(bookingResponseDto);

        String result = mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputBookingDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertEquals(expectedDto, result);
    }

    @Test
    @SneakyThrows
    void createBooking_whenBookerNotFound_thenStatusNotFound() {
        Mockito.when(bookingService.createNewBooking(anyInt(), any()))
                .thenThrow(new ObjectNotFoundException(String.format("Пользователь с id=%d не найден в БД!", 0)));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id",1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputBookingDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void createBooking_whenItemNotFound_thenStatusNotFound() {
        Mockito.when(bookingService.createNewBooking(anyInt(), any()))
                .thenThrow(new ObjectNotFoundException(String.format("Вещь с id=%d не найдена в БД!", 0)));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id",1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputBookingDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void createBooking_whenStartBookingDateIncorrect_thenStatusBadRequest() {
        LocalDateTime startBookingDate = LocalDateTime.now().minusMinutes(5);
        inputBookingDto.setStart(startBookingDate);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id",1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputBookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void createBooking_whenEndBookingDateIncorrect_thenStatusBadRequest() {
        LocalDateTime endBookingDate = LocalDateTime.now().minusDays(1);
        inputBookingDto.setEnd(endBookingDate);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id",1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputBookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void approveOrRejectBooking_whenItemOwnerApprovedBooking_thenStatusOkAndHasBodyResponse() {
        String status = "APPROVED";
        bookingResponseDto.setStatus(status);
        String expectedDto = objectMapper.writeValueAsString(bookingResponseDto);
        Mockito.when(bookingService.approveOrRejectBooking(anyInt(), anyInt(), anyBoolean()))
                .thenReturn(bookingResponseDto);

        String result = mockMvc.perform(patch("/bookings/{bookingId}",1)
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertEquals(expectedDto, result);
    }

    @Test
    @SneakyThrows
    void approveOrRejectBooking_whenBookingNotFound_thenStatusNotFound() {
        Mockito.when(bookingService.approveOrRejectBooking(anyInt(), anyInt(), anyBoolean()))
                .thenThrow(new ObjectNotFoundException(String.format("Бронирование с id=%d не найдено в БД!", 0)));

        mockMvc.perform(patch("//bookings/{bookingId}", 0)
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void approveOrRejectBooking_whenUserNotFound_thenStatusNotFound() {
        Mockito.when(bookingService.approveOrRejectBooking(anyInt(), anyInt(), anyBoolean()))
                .thenThrow(new ObjectNotFoundException(String.format("Пользователь с id=%d не найден!", 0)));

        mockMvc.perform(patch("//bookings/{bookingId}", 1)
                        .header("X-Sharer-User-Id", 0)
                        .param("approved", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void getBooking_whenInvoke_thenStatusOkAndHasBodyResponse() {
        String expectedDto = objectMapper.writeValueAsString(bookingResponseDto);
        Mockito.when(bookingService.getBooking(anyInt(), anyInt())).thenReturn(bookingResponseDto);

        String result = mockMvc.perform(get("/bookings/{bookingId}",1)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertEquals(expectedDto, result);
    }

    @Test
    @SneakyThrows
    void getBooking_whenBookingNotFound_thenStatusNotFound() {
        Mockito.when(bookingService.getBooking(anyInt(), anyInt()))
                .thenThrow(new ObjectNotFoundException(String.format("Бронирование с id=%d не найдено в БД!", 0)));

        mockMvc.perform(get("/bookings/{bookingId}", 0)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void getBooking_whenUserNotFound_thenStatusNotFound() {
        Mockito.when(bookingService.getBooking(anyInt(), anyInt()))
                .thenThrow(new ObjectNotFoundException(String.format("Пользователь с id=%d не найден!", 0)));

        mockMvc.perform(get("/bookings/{bookingId}", 1)
                        .header("X-Sharer-User-Id", 0))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void getBooking_whenUserNotBookerAndNotItemOwner_thenStatusNotFound() {
        Mockito.when(bookingService.getBooking(anyInt(), anyInt()))
                .thenThrow(new BookingException(String.format(
                        "У пользователя с id=%d нет права на просмотр бронирования с id=%d", 3, 1)));

        mockMvc.perform(get("/bookings/{bookingId}", 1)
                        .header("X-Sharer-User-Id", 3))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void getBookingsForBooker_whenInvoke_thenStatusOkAndHasBodyResponse() {
        List<BookingResponseDto> answers = List.of(bookingResponseDto);
        String expectedDto = objectMapper.writeValueAsString(answers);
        Mockito.when(bookingService.getBookingsForBooker(anyInt(), anyString(), anyInt(), anyInt()))
                .thenReturn(answers);

        String result = mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertEquals(expectedDto, result);
    }

    @Test
    @SneakyThrows
    void getBookingsForBooker_whenBookerNotFound_thenStatusNotFound() {
        Mockito.when(bookingService.getBookingsForBooker(anyInt(), anyString(), anyInt(), anyInt()))
                .thenThrow(new ObjectNotFoundException(String.format("Пользователь с id=%d не найден!", 0)));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 0)
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void getBookingsForBooker_whenIncorrectParamState_thenStatusBadRequest() {
        String state = "EAGER";
        Mockito.when(bookingService.getBookingsForBooker(anyInt(), anyString(), anyInt(), anyInt()))
                .thenThrow(new UnknownStateException(String.format("Unknown state: %s", state)));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", state)
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void getBookingsForBooker_whenIncorrectParamFrom_thenStatusBadRequest() {
        String from = "-1";

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", from)
                        .param("size", "2"))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingService, never()).getBookingsForBooker(anyInt(), anyString(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void getBookingsForBooker_whenIncorrectParamSize_thenStatusBadRequest() {
        String size = "-1";

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", size))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingService, never()).getBookingsForBooker(anyInt(), anyString(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void getBookingsForItemsOwner_whenInvoke_thenStatusOkAndHasBodyResponse() {
        List<BookingResponseDto> answers = List.of(bookingResponseDto);
        String expectedDto = objectMapper.writeValueAsString(answers);
        Mockito.when(bookingService.getBookingsForItemsOwner(anyInt(), anyString(), anyInt(), anyInt()))
                .thenReturn(answers);

        String result = mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertEquals(expectedDto, result);
    }

    @Test
    @SneakyThrows
    void getBookingsForItemsOwner_whenItemOwnerNotFound_thenStatusNotFound() {
        Mockito.when(bookingService.getBookingsForItemsOwner(anyInt(), anyString(), anyInt(), anyInt()))
                .thenThrow(new ObjectNotFoundException(String.format("Пользователь с id=%d не найден!", 0)));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 0)
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void getBookingsForItemsOwner_whenIncorrectParamState_thenStatusBadRequest() {
        String state = "EAGER";
        Mockito.when(bookingService.getBookingsForItemsOwner(anyInt(), anyString(), anyInt(), anyInt()))
                .thenThrow(new UnknownStateException(String.format("Unknown state: %s", state)));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", state)
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void getBookingsForItemsOwner_whenIncorrectParamFrom_thenStatusBadRequest() {
        String from = "-1";

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", from)
                        .param("size", "2"))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingService, never()).getBookingsForItemsOwner(anyInt(), anyString(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void getBookingsForItemsOwner_whenIncorrectParamSize_thenStatusBadRequest() {
        String size = "-1";

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", size))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingService, never()).getBookingsForItemsOwner(anyInt(), anyString(), anyInt(), anyInt());
    }
}

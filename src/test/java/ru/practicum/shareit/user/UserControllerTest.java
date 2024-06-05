package ru.practicum.shareit.user;

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
import ru.practicum.shareit.exception.EmailValidationException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    private UserDto inputUserDto;
    private UserDto userResponseDto;

    @BeforeEach
    void setUp() {
        inputUserDto = new UserDto(null, "User", "User@mail.ru");
        userResponseDto = new UserDto(1, "User", "User@mail.ru");
    }

    @Test
    @SneakyThrows
    void createUser_whenInvoke_thenStatusCreatedAndHasBodyResponse() {
        String expectedDto = objectMapper.writeValueAsString(userResponseDto);
        Mockito.when(userService.createUser(any())).thenReturn(userResponseDto);

        String result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUserDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertEquals(expectedDto, result);
    }

    @Test
    @SneakyThrows
    void createUser_whenEmailIncorrect_thenStatusBadRequest() {
        inputUserDto.setEmail("Usermail.ru");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUserDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(userService, never()).createUser(any());
    }

    @Test
    @SneakyThrows
    void createUser_whenAddAnotherUserWithDuplicateEmail_thenStatusConflict() {
        Mockito.when(userService.createUser(any()))
                .thenThrow(new EmailValidationException(
                        String.format("Пользователь с почтой '%s' уже зарегистрирован!", inputUserDto.getEmail())));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUserDto)))
                .andExpect(status().isConflict());
    }

    @Test
    @SneakyThrows
    void updateUser_whenUserFound_thenStatusOkAndHasBodyResponse() {
        UserDto inputUpdatedUser = new UserDto(null, "User", "User@gmail.com");
        userResponseDto.setEmail("User@gmail.com");
        String expectedUserDto = objectMapper.writeValueAsString(userResponseDto);
        Mockito.when(userService.updateUser(anyInt(), any())).thenReturn(userResponseDto);

        String result = mockMvc.perform(patch("/users/{userId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUpdatedUser)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertEquals(expectedUserDto, result);
    }

    @Test
    @SneakyThrows
    void updateUser_whenUserNotFound_thenStatusNotFound() {
        int userId = 0;
        inputUserDto.setEmail("User@yandex.ru");
        Mockito.when(userService.updateUser(anyInt(), any()))
                .thenThrow(new ObjectNotFoundException(
                        String.format("Пользователь с id=%d не найден! Операция обновления невозможна", userId)));

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUserDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void getUsers_whenInvoke_thenStatusOkAndHasBodyResponse() {
        List<UserDto> userDtoList = List.of(userResponseDto);
        String expectedDto = objectMapper.writeValueAsString(userDtoList );
        Mockito.when(userService.getUsers()).thenReturn(userDtoList);

        String result = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertEquals(1, userDtoList.size());
        assertEquals(expectedDto, result);
    }

    @Test
    @SneakyThrows
    void getUser_whenUserFound_thenStatusOkAndHasBodyResponse() {
        int userId = 1;
        String expectedDto = objectMapper.writeValueAsString(userResponseDto);
        Mockito.when(userService.getUser(userId)).thenReturn(userResponseDto);

        String result = mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertEquals(expectedDto, result);
    }

    @Test
    @SneakyThrows
    void getUser_whenUserNotFound_thenStatusNotFound() {
        int userId = 0;
        Mockito.when(userService.getUser(userId))
                .thenThrow(new ObjectNotFoundException(String.format("Пользователь с id=%d не найден!", userId)));

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void deleteUser_whenUserFound_thenStatusOk() {
        int userId = 1;
        Mockito.doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void deleteUser_whenUserNotFound_thenStatusNotFound() {
        int userId = 0;
        Mockito.doThrow(new ObjectNotFoundException(
                String.format("Пользователь с id=%d не найден! Операция удаления невозможна", userId)))
                .when(userService).deleteUser(userId);

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isNotFound());
    }
}
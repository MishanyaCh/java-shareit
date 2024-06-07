package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserMapperImpl;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserMapperImplTest {
    UserMapper userMapper = new UserMapperImpl();
    private UserDto inputUserDto;
    private User user;
    private UserDto userResponseDto;

    @BeforeEach
    void setUp() {
        inputUserDto = new UserDto(null, "User", "User@mail.ru");
        user = new User(1, "User", "User@mail.ru");
        userResponseDto = new UserDto(1, "User", "User@mail.ru");
    }

    @Test
    void toUserDto_whenInvoke_thenReturnUser() {
        User expected = new User(0, "User", "User@mail.ru");

        User result = userMapper.toUser(inputUserDto);

        assertNotNull(result);
        assertEquals(0, result.getId());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getEmail(), result.getEmail());
    }

    @Test
    void toUser_whenInvoke_thenReturnUserDto() {
        UserDto expected = new UserDto(1, "User", "User@mail.ru");

        UserDto result = userMapper.toUserDto(user);

        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getEmail(), result.getEmail());
    }

    @Test
    void toUserDtoList_whenInvoke_thenReturnUserDtoList() {
        List<UserDto> expected = List.of(userResponseDto);

        List<UserDto> result = userMapper.toUserDtoList(List.of(user));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(expected.size(), result.size());
    }

    @Test
    void toUserDtoList_whenUsersListIsEmpty_thenReturnEmptyList() {
        List<UserDto> result = userMapper.toUserDtoList(new ArrayList<>());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
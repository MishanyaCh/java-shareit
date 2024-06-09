package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserMapper {
    UserDto toUserDto(User user);

    User toUser(UserDto userDto);

    List<UserDto> toUserDtoList(List<User> users);
}

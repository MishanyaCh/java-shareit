package ru.practicum.shareit.user.Service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);

    UserDto updateUser(int id, UserDto userDto);

    List<UserDto> getUsers();
    
    UserDto getUser(int id);

    void deleteUser(int id);
}

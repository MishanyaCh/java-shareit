package ru.practicum.shareit.user.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }

    @Override
    public User toUser(UserDto userDto) {
        return new User(0, userDto.getName(), userDto.getEmail());
    }

    @Override
    public List<UserDto> toUserDtoList(List<User> users) {
        List<UserDto> dtoList = new ArrayList<>();
        if (users.isEmpty()) {
            return dtoList;
        }
        for (User user : users) {
            UserDto userDto = toUserDto(user);
            dtoList.add(userDto);
        }
        return dtoList;
    }
}

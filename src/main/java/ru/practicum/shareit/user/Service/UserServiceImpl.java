package ru.practicum.shareit.user.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.EmailValidationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.Storage.UserStorage;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserStorage userStorageArg, UserMapper userMapperArg) {
        userStorage = userStorageArg;
        userMapper = userMapperArg;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User newUser = userMapper.toUser(userDto);
        checkEmailDuplicate(newUser);
        User createdUser = userStorage.createUser(newUser);// добавляем новую запись в хеш-таблицу
        return userMapper.toUserDto(createdUser);
    }

    @Override
    public UserDto updateUser(int id, UserDto userDto) {
        User savedUser = userStorage.getUser(id);
        if (savedUser == null) {
            String message = String.format("Пользователь с id=%d не найден! Операция обновления невозможна", id);
            throw new UserNotFoundException(message);
        }
        User user = userMapper.toUser(userDto);
        user.setId(id);
        checkEmailDuplicate(user);
        User updatedUser = userStorage.updateUser(user);// обновляем запись в хеш-таблице
        return userMapper.toUserDto(updatedUser);
    }

    @Override
    public List<UserDto> getUsers() {
        List<User> users = userStorage.getUsers();
        return userMapper.toUserDtoList(users);
    }

    @Override
    public UserDto getUser(int id) {
        User user = userStorage.getUser(id);
        if (user == null) {
            String message = String.format("Пользователь с id=%d не найден!", id);
            throw new UserNotFoundException(message);
        }
        return userMapper.toUserDto(user);
    }

    @Override
    public void deleteUser(int id) {
        User user = userStorage.getUser(id);
        if (user == null) {
            String message = String.format("Пользователь с id=%d не найден! Операция удаления невозможна", id);
            throw new UserNotFoundException(message);
        }
        userStorage.deleteUser(id);
    }

    private void checkEmailDuplicate(User user) {
        String checkedEmail = user.getEmail();
        int id = user.getId();

        if (checkedEmail == null) {
            return;
        }

        List<User> usersList = userStorage.getUsers();
        for (User u: usersList) {
            if (id == u.getId()) {
                continue;
            }
            if (checkedEmail.equals(u.getEmail())) {
                String message = String.format("Пользователь с почтой '%s' уже зарегистрирован!", checkedEmail);
                throw new EmailValidationException(message);
            }
        }
    }
}

package ru.practicum.shareit.user.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.EmailValidationException;
import ru.practicum.shareit.user.Storage.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepositoryArg, UserMapper userMapperArg) {
        userRepository = userRepositoryArg;
        userMapper = userMapperArg;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User newUser = userMapper.toUser(userDto);
        checkEmailDuplicate(newUser);
        User createdUser = userRepository.save(newUser);// добавляем новую запись в таблицу users
        return userMapper.toUserDto(createdUser);
    }

    @Override
    public UserDto updateUser(int id, UserDto userDto) {
        Optional<User> optionalSavedUser = userRepository.findById(id);
        if (optionalSavedUser.isEmpty()) {
            String message = String.format("Пользователь с id=%d не найден! Операция обновления невозможна", id);
            throw new UserNotFoundException(message);
        }
        // получаем сохраненного в таблице users пользователя, данные которого нужно обновить
        User savedUser = optionalSavedUser.get();
        User user = userMapper.toUser(userDto);// получаем обновленные данные пользователя, которые нужно обновить в БД
        checkEmailDuplicate(user);
        User updatedUser = updateUserInDb(savedUser, user);// обновляем запись в таблице users
        return userMapper.toUserDto(updatedUser);
    }

    @Override
    public List<UserDto> getUsers() {
        List<User> users = userRepository.findAll();// получаем список всех пользователей из таблицы users
        return userMapper.toUserDtoList(users);
    }

    @Override
    public UserDto getUser(int id) {
        Optional<User> optionalUser = userRepository.findById(id);// получаем объект типа Optional
        if (optionalUser.isEmpty()) {
            String message = String.format("Пользователь с id=%d не найден!", id);
            throw new UserNotFoundException(message);
        }
        User user = optionalUser.get();// получаем значение содержащиеся в optionalUser
        return userMapper.toUserDto(user);
    }

    @Override
    public void deleteUser(int id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            String message = String.format("Пользователь с id=%d не найден! Операция удаления невозможна", id);
            throw new UserNotFoundException(message);
        }
        userRepository.deleteById(id);// удаляем запись в таблице users
    }

    private void checkEmailDuplicate(User user) {
        String checkedEmail = user.getEmail();
        int id = user.getId();

        if (checkedEmail == null) {
            return;
        }

        List<User> usersList = userRepository.findAll();
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

    private User updateUserInDb(User savedUser, User updatedDataForUser) {
        String updatedName = updatedDataForUser.getName();
        String updatedEmail = updatedDataForUser.getEmail();

        if (updatedName != null) {
            savedUser.setName(updatedName);
        }
        if (updatedEmail != null) {
            savedUser.setEmail(updatedEmail);
        }
        return userRepository.save(savedUser);
    }
}

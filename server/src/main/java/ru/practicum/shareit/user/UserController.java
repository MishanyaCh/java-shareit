package ru.practicum.shareit.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    @Autowired
    public UserController(UserService userServiceArg) {
        userService = userServiceArg;
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public UserDto createUser(@RequestBody UserDto userDto) {
        log.info("Пришел POST /users запрос с телом: {}", userDto);
        final UserDto createdUser = userService.createUser(userDto);
        log.info("На запрос POST /users отправлен ответ c телом: {}", createdUser);
        return createdUser;
    }

    @PatchMapping(path = "/{userId}")
    public UserDto updateUser(@PathVariable int userId, @RequestBody UserDto userDto) {
        log.info("Пришел PATCH /users/{} запрос с телом: {}", userId, userDto);
        final UserDto updatedUser = userService.updateUser(userId, userDto);
        log.info("На запрос Patch /users/{} отправлен ответ с телом: {}", userId, updatedUser);
        return updatedUser;
    }

    @GetMapping
    public List<UserDto> getUsers() {
        log.info("Пришел GET /users запрос");
        final List<UserDto> users = userService.getUsers();
        log.info("На запрос GET /users отправлен ответ с размером тела: {}", users.size());
        return users;
    }

    @GetMapping(path = "/{userId}")
    public UserDto getUser(@PathVariable int userId) {
        log.info("Пришел GET /users/{} запрос", userId);
        final UserDto user = userService.getUser(userId);
        log.info("На запрос GET /users/{} отправлен ответ с телом: {}", userId, user);
        return user;
    }

    @DeleteMapping(path = "/{userId}")
    public void deleteUser(@PathVariable int userId) {
        log.info("Пришел DELETE /users/{} запрос", userId);
        userService.deleteUser(userId);
        log.info("Пользователь с id={} успешно удален", userId);
    }
}

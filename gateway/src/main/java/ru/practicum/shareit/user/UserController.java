package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserClient userClient;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserDto userDto) {
        log.info("Пришел POST /users запрос с телом: {}", userDto);
        ResponseEntity<Object> response = userClient.createUser(userDto);
        log.info("На запрос POST /users отправлен ответ c телом: {}", response);
        return response;
    }

    @PatchMapping(path = "/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable Long userId, @RequestBody UserDto userDto) {
        log.info("Пришел PATCH /users/{} запрос с телом: {}", userId, userDto);
        ResponseEntity<Object> response = userClient.updateUser(userId, userDto);
        log.info("На запрос Patch /users/{} н ответ с телом: {}", userId, response);
        return response;
    }

    @GetMapping
    public ResponseEntity<Object> getUsers() {
        log.info("Пришел GET /users запрос");
        ResponseEntity<Object> response = userClient.getUsers();
        log.info("На запрос GET /users отправлен ответ  с телом: {}", response);
        return response;
    }

    @GetMapping(path = "/{userId}")
    public ResponseEntity<Object> getUser(@PathVariable Long userId) {
        log.info("Пришел GET /users/{} запрос", userId);
        ResponseEntity<Object> response = userClient.getUser(userId);
        log.info("На запрос GET /users/{} отправлен ответ с телом: {}", userId, response);
        return response;
    }

    @DeleteMapping(path = "/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        log.info("Пришел DELETE /users/{} запрос", userId);
        userClient.deleteUser(userId);
        log.info("Пользователь с id={} успешно удален", userId);
    }
}

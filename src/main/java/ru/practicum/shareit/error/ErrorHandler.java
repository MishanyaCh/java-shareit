package ru.practicum.shareit.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.user.UserController;

import java.util.Map;

@RestControllerAdvice(assignableTypes = {ItemController.class, UserController.class})
public class ErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public Map<String, String> handleEmailValidationException(final EmailValidationException exc) {
        log.error("Отловлена ошибка:" + exc.getMessage());
        return Map.of("Error", exc.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public Map<String, String> handleUserNotFoundException(final UserNotFoundException exc) {
        log.error("Отловлена ошибка:" + exc.getMessage());
        return Map.of("Error", exc.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public Map<String, String> handleItemNotFoundException(final ItemNotFoundException exc) {
        log.error("Отловлена ошибка:" + exc.getMessage());
        return Map.of("Error", exc.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotItemOwnerException(final NotItemOwnerException exc) {
        log.error("Отловлена ошибка:" + exc.getMessage());
        return Map.of("Error", exc.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Map<String, String> handleHeaderNotFoundException(final HeaderNotFoundException exc) {
        log.error("Отловлена ошибка:" + exc.getMessage());
        return Map.of("Error", exc.getMessage());
    }
}

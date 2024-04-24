package ru.practicum.shareit.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.user.UserController;

import java.util.Map;

@RestControllerAdvice(assignableTypes = {ItemController.class, UserController.class, BookingController.class})
public class ErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public Map<String, String> handleEmailValidationException(final EmailValidationException exc) {
        log.error('\n' + "Отловлена ошибка: " + exc.getMessage() + '\n');
        return Map.of("Error", exc.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public Map<String, String> handleObjectNotFoundException(final ObjectNotFoundException exc) {
        log.error('\n' + "Отловлена ошибка: " + exc.getMessage() + '\n');
        return Map.of("Error", exc.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotItemOwnerException(final NotItemOwnerException exc) {
        log.error('\n' + "Отловлена ошибка: " + exc.getMessage() + '\n');
        return Map.of("Error", exc.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Map<String, String> handleHeaderNonexistentException(final HeaderNonexistentException exc) {
        log.error('\n' + "Отловлена ошибка: " + exc.getMessage() + '\n');
        return Map.of("Error", exc.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorResponse handleObjectNotAvailableException(final ObjectNotAvailableException exc) {
        log.error('\n' + "Отловлена ошибка: " + exc.getMessage() + '\n');
        return new ErrorResponse(exc.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDateTimeBookingException(final DateTimeBookingException exc) {
        log.error('\n' + "Отловлена ошибка: " + exc.getMessage() + '\n');
        return new ErrorResponse(exc.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnknownStateException(final UnknownStateException exc) {
        log.error('\n' + "Отловлена ошибка: " + exc.getMessage() + '\n');
        return new ErrorResponse(exc.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorResponse BookingException(final BookingException exc) {
        log.error('\n' + "Отловлена ошибка: " + exc.getMessage() + '\n');
        return new ErrorResponse(exc.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBookingIsApprovedException(final BookingIsApprovedException exc) {
        log.error('\n' + "Отловлена ошибка: " + exc.getMessage() + '\n');
        return new ErrorResponse(exc.getMessage());
    }
}

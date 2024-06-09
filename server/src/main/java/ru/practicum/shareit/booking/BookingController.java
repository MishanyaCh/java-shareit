package ru.practicum.shareit.booking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private static final Logger log = LoggerFactory.getLogger(BookingController.class);
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingServiceArg) {
        bookingService = bookingServiceArg;
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public BookingResponseDto createBooking(@RequestHeader(value = "X-Sharer-User-Id") Integer userId,
                                            @RequestBody BookingCreateRequestDto bookingDto) {
        log.info("Пришел POST /bookings запрос с заголовком 'X-Sharer-User-Id' и телом: " +
                '\n' + "Содержимое 'X-Sharer-User-Id': {}" + '\n' + "Тело: {}", userId, bookingDto);
        final BookingResponseDto createdBooking = bookingService.createNewBooking(userId, bookingDto);
        log.info("На POST /bookings запрос отправлен ответ с телом: {}", createdBooking);
        return createdBooking;
    }

    @PatchMapping(path = "/{bookingId}")
    public BookingResponseDto approveOrRejectBooking(@RequestHeader(value = "X-Sharer-User-Id") Integer userId,
                                                     @PathVariable int bookingId, @RequestParam boolean approved) {
        log.info("Пришел PATCH /bookings/{}?approved={} запрос с заголовком 'X-Sharer-User-Id'" +
                '\n' + "Содержимое 'X-Sharer-User-Id': {}", bookingId, approved, userId);
        final BookingResponseDto updatedBooking = bookingService.approveOrRejectBooking(userId, bookingId, approved);
        log.info("На PATCH /bookings/{}?approved={} запрос отправлен ответ с телом: {}",
                bookingId, approved, updatedBooking);
        return updatedBooking;
    }

    @GetMapping(path = "/{bookingId}")
    public BookingResponseDto getBooking(@RequestHeader(value = "X-Sharer-User-Id") Integer userId,
                                         @PathVariable int bookingId) {
        log.info("Пришел GET /bookings/{} запрос с заголовком 'X-Sharer-User-Id'" +
                '\n' + "Содержимое 'X-Sharer-User-Id': {}", bookingId, userId);
        final BookingResponseDto booking = bookingService.getBooking(userId, bookingId);
        log.info("На GET /bookings/{} запрос отправлен ответ с телом: {}", bookingId, booking);
        return booking;
    }

    @GetMapping
    public List<BookingResponseDto> getBookingsForBooker(
            @RequestHeader(value = "X-Sharer-User-Id") Integer userId,
            @RequestParam(required = false, defaultValue = "ALL") String state,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Пришел GET /bookings?state={}&from={}&size={} запрос с заголовком 'X-Sharer-User-Id'. " +
                '\n' + "Содержимое заголовка 'X-Sharer-User-Id': {}", state, from, size, userId);
        final List<BookingResponseDto> bookingsList = bookingService.getBookingsForBooker(userId, state, from, size);
        log.info("На GET /bookings запрос отправлен ответ c размером тела: {}", bookingsList.size());
        return bookingsList;
    }

    @GetMapping(path = "/owner")
    public List<BookingResponseDto> getBookingsForOwner(
            @RequestHeader(value = "X-Sharer-User-Id") Integer userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Пришел GET /bookings/owner?state={}&from={}&size={} запрос с заголовком 'X-Sharer-User-Id'" +
                '\n' + "Содержимое заголовка 'X-Sharer-User-Id': {}", state, from, size, userId);
        final List<BookingResponseDto> bookingsList = bookingService.getBookingsForItemsOwner(userId, state, from, size);
        log.info("На GET /bookings/owner запрос отправлен ответ с размером тела: {}", bookingsList.size());
        return bookingsList;
    }
}

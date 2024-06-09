package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.PositiveOrZero;

@Validated
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private static final Logger log = LoggerFactory.getLogger(BookingController.class);
    private final BookingClient bookingClient;


    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public ResponseEntity<Object> createBooking(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                                @Valid @RequestBody BookingCreateRequestDto bookingDto) {
        log.info("Пришел POST /bookings запрос с заголовком 'X-Sharer-User-Id' и телом: " +
                '\n' + "Содержимое 'X-Sharer-User-Id': {}" + '\n' + "Тело: {}", userId, bookingDto);
        ResponseEntity<Object> response = bookingClient.createBooking(userId, bookingDto);
        log.info("На POST /bookings запрос отправлен ответ с телом: {}", response);
        return response;
    }

    @PatchMapping(path = "/{bookingId}")
    public ResponseEntity<Object> approveOrRejectBooking(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                                         @PathVariable Long bookingId, @RequestParam Boolean approved) {
        log.info("Пришел PATCH /bookings/{}?approved={} запрос с заголовком 'X-Sharer-User-Id'" +
                '\n' + "Содержимое 'X-Sharer-User-Id': {}", bookingId, approved, userId);
        ResponseEntity<Object> response = bookingClient.approveOrRejectBooking(userId, bookingId, approved);
        log.info("На PATCH /bookings/{}?approved={} запрос отправлен ответ с телом: {}",
                bookingId, approved, response);
        return response;
    }

    @GetMapping(path = "/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                             @PathVariable Long bookingId) {
        log.info("Пришел GET /bookings/{} запрос с заголовком 'X-Sharer-User-Id'" +
                '\n' + "Содержимое 'X-Sharer-User-Id': {}", bookingId, userId);
        ResponseEntity<Object> response = bookingClient.getBooking(userId, bookingId);
        log.info("На GET /bookings/{} запрос отправлен ответ с телом: {}", bookingId, response);
        return response;
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsForBooker(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @RequestParam(required = false, defaultValue = "ALL") String state,
            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(25) Integer size) {
        log.info("Пришел GET /bookings?state={}&from={}&size={} запрос с заголовком 'X-Sharer-User-Id'. " +
                '\n' + "Содержимое заголовка 'X-Sharer-User-Id': {}", state, from, size, userId);
        ResponseEntity<Object> response = bookingClient.getBookingsForBooker(userId, state, from, size);
        log.info("На GET /bookings запрос отправлен ответ c размером тела: {}", response);
        return response;
    }

    @GetMapping(path = "/owner")
    public ResponseEntity<Object> getBookingsForOwner(
            @RequestHeader(value = "X-Sharer-User-Id") Integer userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(25) Integer size) {
        log.info("Пришел GET /bookings/owner?state={}&from={}&size={} запрос с заголовком 'X-Sharer-User-Id'" +
                '\n' + "Содержимое заголовка 'X-Sharer-User-Id': {}", state, from, size, userId);
        ResponseEntity<Object> response = bookingClient.getBookingsForOwner(userId, state, from, size);
        log.info("На GET /bookings/owner запрос отправлен ответ с размером тела: {}", response);
        return response;
    }
}

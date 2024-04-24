package ru.practicum.shareit.booking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.Service.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoForCreateBooking;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
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
    public BookingDto createBooking(@RequestHeader(value = "X-Sharer-User-Id") Integer userId,
                                    @Valid @RequestBody BookingDtoForCreateBooking bookingDto) {
        log.info('\n' + "Пришел POST /bookings запрос с заголовком 'X-Sharer-User-Id' и телом: " +
                '\n' + "Содержимое 'X-Sharer-User-Id': {}" + '\n' + "Тело: {}", userId, bookingDto);
        final BookingDto createdBooking = bookingService.createNewBooking(userId, bookingDto);
        log.info('\n' + "На POST /bookings запрос отправлен ответ с телом: {}", createdBooking);
        return createdBooking;
    }

    @PatchMapping(path = "/{bookingId}")
    public BookingDto approveOrRejectBooking(@RequestHeader(value = "X-Sharer-User-Id") Integer userId,
                                             @PathVariable int bookingId, @RequestParam boolean approved) {
        log.info('\n' + "Пришел PATCH /bookings/{}?approved={} запрос с заголовком 'X-Sharer-User-Id'" +
                '\n' + "Содержимое 'X-Sharer-User-Id': {}", bookingId, approved, userId);
        final BookingDto updatedBooking = bookingService.approveOrRejectBooking(userId, bookingId, approved);
        log.info('\n' + "На PATCH /bookings/{}?approved={} запрос отправлен ответ с телом: {}",
                bookingId, approved, updatedBooking);
        return updatedBooking;
    }

    @GetMapping(path = "/{bookingId}")
    public BookingDto getBooking(@RequestHeader(value = "X-Sharer-User-Id") Integer userId,
                                 @PathVariable int bookingId) {
        log.info('\n' + "Пришел GET /bookings/{} запрос с заголовком 'X-Sharer-User-Id'" +
                '\n' + "Содержимое 'X-Sharer-User-Id': {}", bookingId, userId);
        final BookingDto booking = bookingService.getBooking(userId, bookingId);
        log.info('\n' + "На GET /bookings/{} запрос отправлен ответ с телом: {}", bookingId, booking);
        return booking;
    }

    @GetMapping
    public List<BookingDto> getBookingsForBooker(@RequestHeader(value = "X-Sharer-User-Id") Integer userId,
                                        @RequestParam(defaultValue = "ALL") String state) {
        log.info('\n' + "Пришел GET /bookings?state={} запрос с заголовком 'X-Sharer-User-Id'" +
                '\n' + "Содержимое 'X-Sharer-User-Id': {}", state, userId);
        final List<BookingDto> bookingsList = bookingService.getBookingsForBooker(userId, state);
        log.info('\n' + "На GET /bookings запрос отправлен ответ c размером тела: {}", bookingsList.size());
        return bookingsList;
    }

    @GetMapping(path = "/owner")
    public List<BookingDto> getBookingsForOwner(@RequestHeader(value = "X-Sharer-User-Id") Integer userId,
                                                @RequestParam(defaultValue = "ALL") String state) {
        log.info('\n' + "Пришел GET /bookings/owner?state={} запрос с заголовком 'X-Sharer-User-Id'" +
                '\n' + "Содержимое 'X-Sharer-User-Id': {}", state, userId);
        final List<BookingDto> bookingsList = bookingService.getBookingsForItemsOwner(userId, state);
        log.info('\n' + "На GET /bookings/owner запрос отправлен ответ с размером тела: {}", bookingsList.size());
        return bookingsList;
    }
}

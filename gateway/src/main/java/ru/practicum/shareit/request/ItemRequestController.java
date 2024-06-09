package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private static final Logger log = LoggerFactory.getLogger(ItemRequestController.class);

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public ResponseEntity<Object> createItemRequest(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                                    @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Пришел POST /requests запрос с заголовком 'X-Sharer-User-Id' и телом: " + '\n' +
                "Содержимое заголовка 'X-Sharer-User-Id': {}" + '\n' + "Содержимое тела: {}", userId, itemRequestDto);
        ResponseEntity<Object> response = itemRequestClient.createItemRequest(userId, itemRequestDto);
        log.info("На POST /requests запрос отправлен ответ с телом: {}", response);
        return response;
    }

    @GetMapping
    public ResponseEntity<Object> getRequestsWithAnswersForRequester(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        log.info("Пришел GET /requests запрос с заголовком 'X-Sharer-User-Id'. " + '\n' +
                "Содержимое заголовка 'X-Sharer-User-Id': {}", userId);
        ResponseEntity<Object> response = itemRequestClient.getRequestsWithItemsForRequester(userId);
        log.info("На GET /requests запрос отправлен ответ с размером тела: {}", response);
        return response;
    }

    @GetMapping(path = "/all")
    public ResponseEntity<Object> getOtherRequestsWithAnswers(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Long from,
            @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(25) Long size) {
        log.info("Пришел GET /requests/all&from={}&size={} запрос c заголовком 'X-Sharer-User-Id'. " +
                '\n' + "Содержимое заголовка 'X-Sharer-User-Id': {}", from, size, userId);
        ResponseEntity<Object> response = itemRequestClient.getOtherRequestsWithItems(userId, from, size);
        log.info("На GET /requests/all&from={}&size={} запрос отправлен ответ с размером тела: {}",
                from, size, response);
        return response;
    }

    @GetMapping(path = "/{requestId}")
    public ResponseEntity<Object> getItemRequest(@PathVariable Long requestId,
                                                 @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        log.info("Пришел GET /requests/{} запрос с заголовком 'X-Sharer-User-Id'. " + '\n' +
                "Содержимое заголовка 'X-Sharer-User-Id': {}", requestId, userId);
        ResponseEntity<Object> response = itemRequestClient.getRequest(userId, requestId);
        log.info("На GET /requests/{} запрос отправлен ответ с телом: {}", requestId, response);
        return response;
    }
}
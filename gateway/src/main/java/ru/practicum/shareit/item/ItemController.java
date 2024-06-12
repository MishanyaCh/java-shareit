package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.PositiveOrZero;

@Validated
@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
public class ItemController {
    private static final Logger log = LoggerFactory.getLogger(ItemController.class);
    private final ItemClient itemClient;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public ResponseEntity<Object> createItem(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                             @Valid @RequestBody ItemDto itemDto) {
        log.info("Пришел POST /items запрос с заголовком 'X-Sharer-User-Id' и телом: " + '\n' +
                "Содержимое заголовка 'X-Sharer-User-Id': {}" + '\n' + "Содержимое тела: {}", userId, itemDto);
        ResponseEntity<Object> response = itemClient.createItem(userId, itemDto);
        log.info("На POST /items запрос отправлен ответ с телом: {}", response);
        return response;
    }

    @PatchMapping(path = "/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                             @PathVariable Long itemId, @RequestBody ItemDto itemDto) {
        log.info("Пришел PATCH /items/{} запрос c заголовком 'X-Sharer-User-Id' и телом: " + '\n' +
                "Содержимое заголовка 'X-Sharer-User-Id': {}" + '\n' + "Содержимое тела: {}", itemId, userId, itemDto);
        ResponseEntity<Object> response = itemClient.updateItem(userId, itemDto, itemId);
        log.info("На PATCH /items/{} запрос отправлен ответ с телом: {}", itemId, response);
        return response;
    }

    @GetMapping
    public ResponseEntity<Object> getAllOwnerItems(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Long from,
            @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(25) Long size) {
        log.info("Пришел GET /items?from={}&size={} запрос c заголовком 'X-Sharer-User-Id': " + '\n' +
                "Содержимое заголовка 'X-Sharer-User-Id': {}", userId, from, size);
        ResponseEntity<Object> response = itemClient.getItems(userId, from, size);
        log.info("На GET /items запрос отправлен ответ с размером тела: {}", response);
        return response;
    }

    @GetMapping(path = "/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                          @PathVariable Long itemId) {
        log.info("Пришел GET /items/{} запрос c заголовком 'X-Sharer-User-Id': " + '\n' +
                "Содержимое заголовка 'X-Sharer-User-Id': {}", itemId, userId);
        ResponseEntity<Object> response = itemClient.getItem(userId, itemId);
        log.info("На GET /items/{} запрос отправлен ответ с телом: {}", itemId, response);
        return response;
    }

    @GetMapping(path = "/search")
    public ResponseEntity<Object> searchItemsForUser(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                                     @RequestParam String text,
                                                     @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Long from,
                                                     @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(25) Long size) {
        log.info("Пришел GET /items/search?text={}&from={}&size={} запрос c заголовком 'X-Sharer-User-Id': " + '\n' +
                "Содержимое заголовка 'X-Sharer-User-Id': {}", text, userId, from, size);
        ResponseEntity<Object> response = itemClient.searchItems(userId, text, from, size);
        log.info("На GET /items/search?text={} запрос отправлен ответ с размером тела: {}", text, response);
        return response;
    }

    @PostMapping(path = "/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                                @PathVariable Long itemId, @Valid @RequestBody CommentDto commentDto) {
        log.info("Пришел POST /items/{}/comment запрос с заголовком 'X-Sharer-User-Id' и телом: " +
                '\n' + "Содержимое заголовка 'X-Sharer-User-Id': {}" +
                '\n' + "Содержимое тела: {}", userId, itemId, commentDto);
        ResponseEntity<Object> response = itemClient.addComment(userId, commentDto, itemId);
        log.info("На POST /items/{}/comment запрос отправлен ответ с телом: {}", itemId, response);
        return response;
    }
}

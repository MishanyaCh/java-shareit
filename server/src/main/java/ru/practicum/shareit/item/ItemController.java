package ru.practicum.shareit.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComment;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/items")
public class ItemController {
    private final ItemService itemService;
    private static final Logger log = LoggerFactory.getLogger(ItemController.class);

    @Autowired
    public ItemController(ItemService itemServiceArg) {
        itemService = itemServiceArg;
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public ItemDto createItem(@RequestHeader(value = "X-Sharer-User-Id") Integer userId,
                              @RequestBody ItemDto itemDto) {
        log.info("Пришел POST /items запрос с заголовком 'X-Sharer-User-Id' и телом: " + '\n' +
                "Содержимое заголовка 'X-Sharer-User-Id': {}" + '\n' + "Содержимое тела: {}", userId, itemDto);
        final ItemDto createdItem = itemService.createItem(userId, itemDto);
        log.info("На POST /items запрос отправлен ответ с телом: {}", createdItem);
        return createdItem;
    }

    @PatchMapping(path = "/{itemId}")
    public ItemDto updateItem(@RequestHeader(value = "X-Sharer-User-Id") Integer userId,
                              @PathVariable int itemId, @RequestBody ItemDto itemDto) {
        log.info("Пришел PATCH /items/{} запрос c заголовком 'X-Sharer-User-Id' и телом: " + '\n' +
                "Содержимое заголовка 'X-Sharer-User-Id': {}" + '\n' + "Содержимое тела: {}", itemId, userId, itemDto);
        final ItemDto updatedItem = itemService.updateItem(userId, itemId, itemDto);
        log.info("На PATCH /items/{} запрос отправлен ответ с телом: {}", itemId, updatedItem);
        return updatedItem;
    }

    @GetMapping
    public List<ItemDtoWithBookingAndComment> getAllOwnerItems(
            @RequestHeader(value = "X-Sharer-User-Id") Integer userId,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Пришел GET /items?from={}&size={} запрос c заголовком 'X-Sharer-User-Id': " + '\n' +
                "Содержимое заголовка 'X-Sharer-User-Id': {}", userId, from, size);
        final List<ItemDtoWithBookingAndComment> items = itemService.getItems(userId, from, size);
        log.info("На GET /items запрос отправлен ответ с размером тела: {}", items.size());
        return items;
    }

    @GetMapping(path = "/{itemId}")
    public ItemDtoWithBookingAndComment getItem(@RequestHeader(value = "X-Sharer-User-Id") Integer userId,
                                                @PathVariable int itemId) {
        log.info("Пришел GET /items/{} запрос c заголовком 'X-Sharer-User-Id': " + '\n' +
                "Содержимое заголовка 'X-Sharer-User-Id': {}", itemId, userId);
        final ItemDtoWithBookingAndComment item = itemService.getItem(itemId, userId);
        log.info("На GET /items/{} запрос отправлен ответ с телом: {}", itemId, item);
        return item;
    }

    @GetMapping(path = "/search")
    public List<ItemDto> searchItemsForUser(@RequestHeader(value = "X-Sharer-User-Id") Integer userId,
            @RequestParam String text,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Пришел GET /items/search?text={}&from={}&size={} запрос c заголовком 'X-Sharer-User-Id': " + '\n' +
                "Содержимое заголовка 'X-Sharer-User-Id': {}", text, userId, from, size);
        final List<ItemDto> itemsList = itemService.searchItems(userId, text, from, size);
        log.info("На GET /items/search?text={} запрос отправлен ответ с размером тела: {}", text, itemsList.size());
        return itemsList;
    }

    @PostMapping(path = "/{itemId}/comment")
    public CommentDto createComment(@RequestHeader(value = "X-Sharer-User-Id") Integer userId,
                                    @PathVariable int itemId, @RequestBody CommentDto commentDto) {
        log.info("Пришел POST /items/{}/comment запрос с заголовком 'X-Sharer-User-Id' и телом: " +
                '\n' + "Содержимое заголовка 'X-Sharer-User-Id': {}" +
                '\n' + "Содержимое тела: {}", userId, itemId, commentDto);
        final CommentDto createdComment = itemService.createComment(userId, itemId, commentDto);
        log.info("На POST /items/{}/comment запрос отправлен ответ с телом: {}", itemId, createdComment);
        return createdComment;
    }
}

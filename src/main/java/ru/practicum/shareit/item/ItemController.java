package ru.practicum.shareit.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.HeaderNonexistentException;
import ru.practicum.shareit.item.Service.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

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
    public ItemDto createItem(@RequestHeader(value = "X-Sharer-User-Id", required = false) Integer userId,
                              @Valid @RequestBody ItemDto itemDto) {
        log.info('\n' + "Пришел POST /items запрос с заголовком 'X-Sharer-User-Id' и телом: " + '\n' +
                "Содержимое 'X-Sharer-User-Id': {}" + '\n' + "Тело: {}", userId, itemDto);
        checkHeaderExistence(userId);
        final ItemDto createdItem = itemService.createItem(userId, itemDto);
        log.info('\n' + "На POST /items запрос отправлен ответ с телом: {}", createdItem);
        return createdItem;
    }

    @PatchMapping(path = "/{itemId}")
    public ItemDto updateItem(@RequestHeader(value = "X-Sharer-User-Id", required = false) Integer userId,
                              @PathVariable int itemId, @RequestBody ItemDto itemDto) {
        log.info('\n' + "Пришел PATCH /items/{} запрос c заголовком 'X-Sharer-User-Id' и телом: " + '\n' +
                "Содержимое 'X-Sharer-User-Id': {}" + '\n' + "Тело: {}", itemId, userId, itemDto);
        checkHeaderExistence(userId);
        final ItemDto updatedItem = itemService.updateItem(userId, itemId, itemDto);
        log.info('\n' + "На PATCH /items/{} запрос отправлен ответ с телом: {}", itemId, updatedItem);
        return updatedItem;
    }

    @GetMapping
    public List<ItemDto> getAllOwnerItems(@RequestHeader(value = "X-Sharer-User-Id", required = false) Integer userId) {
        log.info('\n' + "Пришел GET /items запрос c заголовком 'X-Sharer-User-Id'" + '\n' +
                "Содержимое 'X-Sharer-User-Id': {}", userId);
        checkHeaderExistence(userId);
        final List<ItemDto> items = itemService.getItems(userId);
        log.info('\n' + "На GET /items запрос отправлен ответ с размером тела: {}" + '\n', items.size());
        return items;
    }

    @GetMapping(path = "/{itemId}")
    public ItemDto getItem(@PathVariable int itemId) {
        log.info('\n' + "Пришел GET /items/{} запрос", itemId);
        final ItemDto item = itemService.getItem(itemId);
        log.info('\n' + "На GET /items/{} запрос отправлен ответ с телом: {}" + '\n', itemId, item);
        return item;
    }

    @GetMapping(path = "/search")
    public List<ItemDto> searchItemsForUser(@RequestHeader(value = "X-Sharer-User-Id", required = false) Integer userId,
                                            @RequestParam String text) {
        log.info('\n' + "Пришел GET /items/search?text={} запрос c заголовком 'X-Sharer-User-Id'" + '\n' +
                "Содержимое 'X-Sharer-User-Id': {}", text, userId);
        checkHeaderExistence(userId);
        final List<ItemDto> itemsList = itemService.searchItems(userId, text);
        log.info('\n' + "На GET /items/search?text={} запрос отправлен ответ с размером тела: {}" + '\n',
                text, itemsList.size());
        return itemsList;
    }

    private void checkHeaderExistence(Integer userId) {
        if (userId == null) {
            String message = "В запросе отсутствует ожидаемый заголовок 'X-Sharer-User-Id'! " +
                    "Невозможно выполнить текущий запрос";
            throw new HeaderNonexistentException(message);
        }
    }
}

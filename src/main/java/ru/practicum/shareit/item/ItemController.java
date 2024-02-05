package ru.practicum.shareit.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.HeaderNotFoundException;
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
        checkHeaderExistence(userId);
        log.info("Пришел POST /items запрос с телом: {}", itemDto);
        final ItemDto createdItem = itemService.createItem(userId, itemDto);
        log.info("На POST /items запрос отправлен ответ с телом: {}", createdItem);
        return createdItem;
    }

    @PatchMapping(path = "/{itemId}")
    public ItemDto updateItem(@RequestHeader(value = "X-Sharer-User-Id", required = false) Integer userId,
                              @PathVariable int itemId, @RequestBody ItemDto itemDto) {
        checkHeaderExistence(userId);
        log.info("Пришел PATCH /items/{} запрос с телом: {}", itemId, itemDto);
        final ItemDto updatedItem = itemService.updateItem(userId, itemId, itemDto);
        log.info("На PATCH /items/{} запрос отправлен ответ с телом: {}", itemId, updatedItem);
        return updatedItem;
    }

    @GetMapping
    public List<ItemDto> getAllOwnerItems(@RequestHeader(value = "X-Sharer-User-Id", required = false) Integer userId) {
        checkHeaderExistence(userId);
        log.info("Пришел GET /items запрос");
        final List<ItemDto> items = itemService.getItems(userId);
        log.info("На GET /items запрос отправлен ответ с размером тела: {}", items.size());
        return items;
    }

    @GetMapping(path = "/{itemId}")
    public ItemDto getItem(@PathVariable int itemId) {
        log.info("Пришел GET /items/{} запрос", itemId);
        final ItemDto item = itemService.getItem(itemId);
        log.info("На GET /items/{} запрос отправлен ответ с телом: {}", itemId, item);
        return item;
    }

    @GetMapping(path = "/search")
    public List<ItemDto> searchItemsForUser(@RequestHeader(value = "X-Sharer-User-Id", required = false) Integer userId,
                                            @RequestParam String text) {
        checkHeaderExistence(userId);
        log.info("Пришел GET /items/search?text={} запрос", text);
        final List<ItemDto> itemsList = itemService.searchItems(userId, text);
        log.info("На GET /items/search?text={} запрос отправлен ответ с размером тела: {}", text, itemsList.size());
        return itemsList;
    }

    private void checkHeaderExistence(Integer userId) {
        if (userId == null) {
            String message = "В запросе отсутствует ожидаемый заголовок 'X-Sharer-User-Id'!";
            throw new HeaderNotFoundException(message);
        }
    }
}

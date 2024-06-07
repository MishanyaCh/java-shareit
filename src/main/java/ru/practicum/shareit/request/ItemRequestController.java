package ru.practicum.shareit.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithAnswers;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private static final Logger log = LoggerFactory.getLogger(ItemRequestController.class);
    private final ItemRequestService itemRequestService;

    @Autowired
    public ItemRequestController(ItemRequestService itemRequestServiceArg) {
        itemRequestService = itemRequestServiceArg;
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public ItemRequestDto createItemRequest(@RequestHeader(value = "X-Sharer-User-Id") Integer userId,
                                            @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Пришел POST /requests запрос с заголовком 'X-Sharer-User-Id' и телом: " + '\n' +
                "Содержимое заголовка 'X-Sharer-User-Id': {}" + '\n' + "Содержимое тела: {}", userId, itemRequestDto);
        final ItemRequestDto createdItemRequest = itemRequestService.createItemRequest(userId, itemRequestDto);
        log.info("На POST /requests запрос отправлен ответ с телом: {}", createdItemRequest);
        return createdItemRequest;
    }

    @GetMapping
    public List<ItemRequestDtoWithAnswers> getRequestsWithAnswersForRequester(
            @RequestHeader(value = "X-Sharer-User-Id") Integer userId) {
        log.info("Пришел GET /requests запрос с заголовком 'X-Sharer-User-Id'. " + '\n' +
                "Содержимое заголовка 'X-Sharer-User-Id': {}", userId);
        final List<ItemRequestDtoWithAnswers> result = itemRequestService.getRequestsWithItemsForRequester(userId);
        log.info("На GET /requests запрос отправлен ответ с размером тела: {}", result.size());
        return result;
    }

    @GetMapping(path = "/all")
    public List<ItemRequestDtoWithAnswers> getOtherRequestsWithAnswers(
            @RequestHeader(value = "X-Sharer-User-Id") Integer userId,
            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(25) Integer size) {
        log.info("Пришел GET /requests/all&from={}&size={} запрос c заголовком 'X-Sharer-User-Id'. " +
                '\n' + "Содержимое заголовка 'X-Sharer-User-Id': {}", from, size, userId);
        final List<ItemRequestDtoWithAnswers> result = itemRequestService.getOtherRequestsWithItems(userId, from, size);
        log.info("На GET /requests/all&from={}&size={} запрос отправлен ответ с размером тела: {}",
                from, size, result.size());
        return result;
    }

    @GetMapping(path = "/{requestId}")
    public ItemRequestDtoWithAnswers getItemRequest(@PathVariable int requestId,
            @RequestHeader(value = "X-Sharer-User-Id") Integer userId) {
        log.info("Пришел GET /requests/{} запрос с заголовком 'X-Sharer-User-Id'. " + '\n' +
                "Содержимое заголовка 'X-Sharer-User-Id': {}", requestId, userId);
        final ItemRequestDtoWithAnswers requestWithAnswers = itemRequestService.getRequest(userId, requestId);
        log.info("На GET /requests/{} запрос отправлен ответ с телом: {}", requestId, requestWithAnswers);
        return requestWithAnswers;
    }
}
package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithAnswers;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createItemRequest(int userId, ItemRequestDto itemRequestDto);

    List<ItemRequestDtoWithAnswers> getRequestsWithItemsForRequester(int userId);

    List<ItemRequestDtoWithAnswers> getOtherRequestsWithItems(int userId, int page, int size);

    ItemRequestDtoWithAnswers getRequest(int userId, int requestId);
}

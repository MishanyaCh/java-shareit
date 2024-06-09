package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemRequestMapper {
    ItemRequestDto toItemRequestDto(ItemRequest itemRequest);

    ItemRequest toItemRequest(User requester, ItemRequestDto itemRequestDto);

    ItemRequestDtoWithAnswers toItemRequestDtoWithAnswers(ItemRequest itemRequest, List<Item> answers);
}

package ru.practicum.shareit.item.Service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(int userId, ItemDto itemDto);

    ItemDto updateItem(int userId, int itemId, ItemDto itemDto);

    List<ItemDto> getItems(int userId);

    ItemDto getItem(int itemId);

    List<ItemDto> searchItems(int userId, String text);
}

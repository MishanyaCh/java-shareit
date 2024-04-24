package ru.practicum.shareit.item.Service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComment;

import java.util.List;

public interface ItemService {
    ItemDto createItem(int userId, ItemDto itemDto);

    ItemDto updateItem(int userId, int itemId, ItemDto itemDto);

    List<ItemDtoWithBookingAndComment> getItems(int userId);

    ItemDtoWithBookingAndComment getItem(int itemId, int userId);

    List<ItemDto> searchItems(int userId, String text);

    CommentDto createComment(int bookerId, int itemId, CommentDto commentDto);
}

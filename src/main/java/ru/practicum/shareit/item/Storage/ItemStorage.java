package ru.practicum.shareit.item.Storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item createItem(Item item);

    Item updateItem(Item item);
    
    List<Item> getItems(int userId);

    Item getItem(int itemId);

    List<Item> searchItems(String text);
}

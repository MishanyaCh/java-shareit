package ru.practicum.shareit.item.Storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ItemInMemoryStorageImpl implements ItemStorage {
    private int id = 0;
    private final Map<Integer, Item> items = new HashMap<>(); // хеш-таблица для хранения вещей

    @Override
    public Item createItem(Item item) {
        int itemId = generateId();
        item.setId(itemId);
        items.put(itemId, item);
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        int itemId = item.getId();
        String updatedName = item.getName();
        String updatedDescription = item.getDescription();
        Boolean isAvailable = item.getIsAvailable();

        Item savedItems = items.get(itemId);// получаем сохраненную вещь, данные которой нужно обновить
        if (updatedName != null) {
            savedItems.setName(updatedName);
        }
        if (updatedDescription != null) {
            savedItems.setDescription(updatedDescription);
        }
        if (isAvailable != null) {
            savedItems.setIsAvailable(isAvailable);
        }
        return items.get(itemId);
    }

    @Override
    public List<Item> getItems(int userId) {
        List<Item> userItems = new ArrayList<>();
        for (Item item: items.values()) {
            int id = item.getOwnerId();
            if (id == userId) {
                userItems.add(item);
            }
        }
        return userItems;
    }

    @Override
    public Item getItem(int itemId) {
        return items.get(itemId);
    }

    @Override
    public List<Item> searchItems(String text) {
        String editText = text.toLowerCase();
        List<Item> itemsList = new ArrayList<>();

        for (Item item : items.values()) {
            String name = item.getName();
            String description = item.getDescription();
            Boolean isAvailable = item.getIsAvailable();

            boolean findMatchByName = name.toLowerCase().contains(editText);
            boolean findMatchByDescription = description.toLowerCase().contains(editText);
            if ((findMatchByName || findMatchByDescription) && isAvailable) {
                itemsList.add(item);
            }
        }
        return itemsList;
    }

    private int generateId() {
        return ++id;
    }
}

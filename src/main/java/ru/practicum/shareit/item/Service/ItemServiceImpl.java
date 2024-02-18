package ru.practicum.shareit.item.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.NotItemOwnerException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.Storage.ItemStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.Storage.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final ItemMapper itemMapper;
    private final UserStorage userStorage;

    @Autowired
    public ItemServiceImpl(UserStorage userStorageArg, ItemStorage itemStorageArg, ItemMapper itemMapperArg) {
        itemStorage = itemStorageArg;
        itemMapper = itemMapperArg;
        userStorage = userStorageArg;
    }

    @Override
    public ItemDto createItem(int userId, ItemDto itemDto) {
        checkUserExistence(userId);// проверяем наличие пользователя в БД
        Item newItem = itemMapper.toItem(itemDto);
        newItem.setOwnerId(userId);// добавляем id пользователя, т.е привязываем вещь к пользователю
        Item createdItem = itemStorage.createItem(newItem);// добавляем новую запись в хеш-таблицу
        return itemMapper.toItemDto(createdItem);
    }

    @Override
    public ItemDto updateItem(int userId, int itemId, ItemDto itemDto) {
        checkUserExistence(userId);// проверяем наличие пользователя в БД
        checkItemExistence(itemId);// проверяем наличие вещи в БД перед обновлением
        checkItemOwner(userId, itemId);// проверяем является ли пользователь владельцем вещи
        Item item = itemMapper.toItem(itemDto);
        item.setId(itemId);// добавляем id
        Item updatedItem = itemStorage.updateItem(item);// обновляем запись в хеш-таблице
        return itemMapper.toItemDto(updatedItem);
    }

    @Override
    public List<ItemDto> getItems(int userId) {
        List<Item> items = itemStorage.getItems(userId);
        return itemMapper.toItemDtoList(items);
    }

    @Override
    public ItemDto getItem(int itemId) {
        checkItemExistence(itemId);
        Item item = itemStorage.getItem(itemId);
        return itemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> searchItems(int userId, String text) {
        checkUserExistence(userId);// проверяем наличие пользователя в БД
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        List<Item> items = itemStorage.searchItems(text);
        return itemMapper.toItemDtoList(items);
    }

    private void checkUserExistence(int userId) {
        User user = userStorage.getUser(userId);
        if (user == null) {
            String message = String.format("Пользователь с id=%d не найден!", userId);
            throw new UserNotFoundException(message);
        }
    }

    private void checkItemExistence(int itemId) {
        Item item = itemStorage.getItem(itemId);
        if (item == null) {
            String message = String.format("Вещь с id=%d не найдена!", itemId);
            throw new ItemNotFoundException(message);
        }
    }

    private void checkItemOwner(int userId, int itemId) {
        Item item = itemStorage.getItem(itemId);
        int ownerId = item.getOwnerId();
        if (ownerId != userId) {
            String message = String.format("Пользователь с id=%d не является владельцем вещи с id=%d! " +
                    "Операция обновления данных невозможна", userId, itemId);
            throw new NotItemOwnerException(message);
        }
    }
}

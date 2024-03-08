package ru.practicum.shareit.item.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.NotItemOwnerException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.Storage.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.Storage.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserRepository userRepository;

    @Autowired
    public ItemServiceImpl(UserRepository userRepositoryArg, ItemRepository itemRepositoryArg,
                           ItemMapper itemMapperArg) {
        itemRepository = itemRepositoryArg;
        itemMapper = itemMapperArg;
        userRepository = userRepositoryArg;
    }

    @Override
    public ItemDto createItem(int userId, ItemDto itemDto) {
        checkUserExistence(userId);// проверяем наличие пользователя в БД
        Item newItem = itemMapper.toItem(itemDto);
        newItem.setOwnerId(userId);// добавляем id пользователя, т.е привязываем вещь к пользователю
        Item createdItem = itemRepository.save(newItem);// добавляем новую запись в таблицу items
        return itemMapper.toItemDto(createdItem);
    }

    @Override
    public ItemDto updateItem(int userId, int itemId, ItemDto itemDto) {
        Optional<Item> optionalSavedItem = itemRepository.findById(itemId);
        if (optionalSavedItem.isEmpty()) {
            String message = String.format("Вещь с id=%d не найдена!", itemId);
            throw new ItemNotFoundException(message);
        }
        // получаем сохраненную в таблице items вещь, данные которой нужно обновить
        Item savedItem = optionalSavedItem.get();
        checkUserExistence(userId);// проверяем наличие пользователя в БД
        checkItemOwner(userId, savedItem);// проверяем является ли пользователь владельцем вещи

        Item item = itemMapper.toItem(itemDto);// получаем обновленные данные вещи, которые нужно обновить в БД
        Item updatedItem = updateItemInDb(savedItem, item);// обновляем запись в таблицу items
        return itemMapper.toItemDto(updatedItem);
    }

    @Override
    public List<ItemDto> getItems(int userId) {
        List<Item> items = itemRepository.findAll();
        return itemMapper.toItemDtoList(items);
    }

    @Override
    public ItemDto getItem(int itemId) {
        Optional<Item> optionalItem = itemRepository.findById(itemId); // получаем объект типа Optional
        if (optionalItem.isEmpty()) {
            String message = String.format("Вещь с id=%d не найдена!", itemId);
            throw new ItemNotFoundException(message);
        }
        Item item = optionalItem.get();// получаем значение содержащиеся в optionalItem
        return itemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> searchItems(int userId, String text) {
        checkUserExistence(userId);// проверяем наличие пользователя в БД
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        String editText = text.toLowerCase();// приводим текст к нижнему регистру
        List<Item> items = itemRepository.searchItems(editText);
        return itemMapper.toItemDtoList(items);
    }

    private void checkUserExistence(int userId) {
        boolean isUserExist = userRepository.existsById(userId);
        if (!isUserExist) {
            String message = String.format("Пользователь с id=%d не найден!", userId);
            throw new UserNotFoundException(message);
        }
    }

    private void checkItemOwner(int userId, Item item) {
        int itemId = item.getId();
        int ownerId = item.getOwnerId();
        if (ownerId != userId) {
            String message = String.format("Пользователь с id=%d не является владельцем вещи с id=%d! " +
                    "Операция обновления данных невозможна", userId, itemId);
            throw new NotItemOwnerException(message);
        }
    }

    private Item updateItemInDb(Item savedItem, Item updatedDateForItem) {
        String updatedName = updatedDateForItem.getName();
        String updatedDescription = updatedDateForItem.getDescription();
        Boolean isAvailable = updatedDateForItem.getIsAvailable();

        if (updatedName != null) {
            savedItem.setName(updatedName);
        }
        if (updatedDescription != null) {
            savedItem.setDescription(updatedDescription);
        }
        if (isAvailable != null) {
            savedItem.setIsAvailable(isAvailable);
        }
        return itemRepository.save(savedItem);
    }
}

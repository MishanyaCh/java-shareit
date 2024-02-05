package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;

@Component
public class ItemMapperImpl implements ItemMapper {

    @Override
    public ItemDto toItemDto(Item item) {
        return new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getIsAvailable(), item.getOwnerId());
    }

    @Override
    public Item toItem(ItemDto itemDto) {
        return new Item(itemDto.getId(), itemDto.getName(), itemDto.getDescription(), itemDto.getIsAvailable(),
                itemDto.getOwnerId());
    }

    @Override
    public List<ItemDto> toItemDtoList(List<Item> items) {
        List<ItemDto> dtoList = new ArrayList<>();
        for (Item item : items) {
            ItemDto itemDto = toItemDto(item);
            dtoList.add(itemDto);
        }
        return dtoList;
    }
}

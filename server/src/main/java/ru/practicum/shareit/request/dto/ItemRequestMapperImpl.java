package ru.practicum.shareit.request.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ItemRequestMapperImpl implements ItemRequestMapper {
    private final ItemMapper itemMapper;

    @Autowired
    public ItemRequestMapperImpl(ItemMapper itemMapperArg) {
        itemMapper = itemMapperArg;
    }

    @Override
    public ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return new ItemRequestDto(itemRequest.getId(), itemRequest.getDescription(), itemRequest.getCreationDate());
    }

    @Override
    public ItemRequest toItemRequest(User requester, ItemRequestDto itemRequestDto) {
        String description = itemRequestDto.getDescription();
        LocalDateTime creationDate = LocalDateTime.now();
        return new ItemRequest(0, description, requester, creationDate);
    }

    @Override
    public ItemRequestDtoWithAnswers toItemRequestDtoWithAnswers(ItemRequest itemRequest, List<Item> answers) {
        int id = itemRequest.getId();
        String description = itemRequest.getDescription();
        LocalDateTime creationDate = itemRequest.getCreationDate();

        List<ItemDto> items = new ArrayList<>();
        if (!answers.isEmpty()) {
            items = itemMapper.toItemDtoList(answers);
        }
        return new ItemRequestDtoWithAnswers(id, description, creationDate, items);
    }
}

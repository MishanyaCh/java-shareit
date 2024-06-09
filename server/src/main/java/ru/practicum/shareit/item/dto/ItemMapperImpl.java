package ru.practicum.shareit.item.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDtoWithoutItemField;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ItemMapperImpl implements ItemMapper {
    private final ItemRequestRepository itemRequestRepository;

    @Autowired
    public ItemMapperImpl(ItemRequestRepository itemRequestRepositoryArg) {
        itemRequestRepository = itemRequestRepositoryArg;
    }

    @Override
    public ItemDto toItemDto(Item item) {
        Integer requestId = null;
        if (item.getRequest() != null) {
            requestId = item.getRequest().getId();
        }
        return new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getIsAvailable(), requestId);
    }

    @Override
    public Item toItem(ItemDto itemDto) {
        Item item = new Item(0, itemDto.getName(), itemDto.getDescription(), itemDto.getIsAvailable(),
                itemDto.getOwnerId());
        Integer requestId = itemDto.getRequestId();
        if (requestId != null) {
            ItemRequest itemRequest = getItemRequest(requestId);
            item.setRequest(itemRequest);
        }
        return item;
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

    @Override
    public ItemDtoWithBookingAndComment toItemDtoWithBookingAndComment(Item item,
                                                                       Booking lastBooking,
                                                                       Booking nextBooking,
                                                                       List<CommentDto> comments) {
        BookingDtoWithoutItemField last = null;
        BookingDtoWithoutItemField next = null;
        Integer requestId = null;

        if (lastBooking != null) {
            last = new BookingDtoWithoutItemField(lastBooking.getId(), lastBooking.getBooker().getId(),
                    lastBooking.getStartBookingDate(), lastBooking.getEndBookingDate());
        }
        if (nextBooking != null) {
            next = new BookingDtoWithoutItemField(nextBooking.getId(), nextBooking.getBooker().getId(),
                    nextBooking.getStartBookingDate(), nextBooking.getEndBookingDate());
        }
        if (item.getRequest() != null) {
            requestId = item.getRequest().getId();
        }
        return new ItemDtoWithBookingAndComment(item.getId(), item.getName(), item.getDescription(),
                item.getIsAvailable(), last, next, comments, requestId);
    }

    private ItemRequest getItemRequest(Integer requestId) {
        Optional<ItemRequest> itemRequestOptional = itemRequestRepository.findById(requestId);
        if (itemRequestOptional.isEmpty()) {
            String message = String.format("Запрос с id=%d на добавление новой вещи не найден!", requestId);
            throw new ObjectNotFoundException(message);
        }
        return itemRequestOptional.get();
    }
}

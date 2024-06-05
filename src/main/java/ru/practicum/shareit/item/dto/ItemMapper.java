package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemMapper {
    ItemDto toItemDto(Item item);

    Item toItem(ItemDto itemDto);

    List<ItemDto> toItemDtoList(List<Item> items);

    ItemDtoWithBookingAndComment toItemDtoWithBookingAndComment(Item item,
                                                                Booking lastBooking,
                                                                Booking nextBooking,
                                                                List<CommentDto> comments);
}

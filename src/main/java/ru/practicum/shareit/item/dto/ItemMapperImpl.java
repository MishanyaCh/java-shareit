package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDtoWithoutItemField;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;

@Component
public class ItemMapperImpl implements ItemMapper {

    @Override
    public ItemDto toItemDto(Item item) {
        return new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getIsAvailable());
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

    @Override
    public ItemDtoWithBookingAndComment toItemDtoWithBookingAndComment(Item item,
                                                                       Booking lastBooking,
                                                                       Booking nextBooking,
                                                                       List<CommentDto> comments) {
        BookingDtoWithoutItemField last = null;
        BookingDtoWithoutItemField next = null;

        if (lastBooking != null) {
            last = new BookingDtoWithoutItemField(lastBooking.getId(), lastBooking.getBooker().getId(),
                    lastBooking.getStartBookingDate(), lastBooking.getEndBookingDate());
        }
        if (nextBooking != null) {
            next = new BookingDtoWithoutItemField(nextBooking.getId(), nextBooking.getBooker().getId(),
                    nextBooking.getStartBookingDate(), nextBooking.getEndBookingDate());
        }
        return new ItemDtoWithBookingAndComment(item.getId(), item.getName(), item.getDescription(),
                item.getIsAvailable(), last, next, comments);
    }
}

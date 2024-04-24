package ru.practicum.shareit.item.dto;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.dto.BookingDtoWithoutItemField;

import java.util.List;

@Getter
@Setter
public class ItemDtoWithBookingAndComment extends ItemDto {
    private BookingDtoWithoutItemField lastBooking;
    private BookingDtoWithoutItemField nextBooking;
    private List<CommentDto> comments;

    public ItemDtoWithBookingAndComment(int idArg, String nameArg, String descriptionArg, Boolean isAvailableArg,
                                        BookingDtoWithoutItemField lastBookingArg,
                                        BookingDtoWithoutItemField nextBookingArg,
                                        List<CommentDto> commentsArg) {
        super(idArg, nameArg, descriptionArg, isAvailableArg);
        lastBooking = lastBookingArg;
        nextBooking = nextBookingArg;
        comments = commentsArg;
    }

    @Override
    public String toString() {
        String result =  "ItemDtoWithBooking{" + "id=" +  getId()+ ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' + ", isAvailable=" + getIsAvailable();

        if (lastBooking != null) {
            result = result + ", " + lastBooking.toString();
        } else {
            result = result + ", lastBooking = 'null'";
        }
        if (nextBooking != null) {
            result = result + ", " + nextBooking.toString();
        } else {
            result = result + ", nextBooking = 'null'";
        }

        return result + "}" + '\n';
    }
}

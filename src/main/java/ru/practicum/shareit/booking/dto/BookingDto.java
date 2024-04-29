package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingDto {
    private int id;

    @JsonProperty("start")
    private LocalDateTime startBookingDate;

    @JsonProperty("end")
    private LocalDateTime endBookingDate;
    private String status;

    @JsonProperty("booker")
    private UserDto bookerDto;

    @JsonProperty("item")
    private ItemDto itemDto;

    public BookingDto(int idArg, LocalDateTime startBookingDateArg, LocalDateTime endBookingDateArg,
                      String statusArg, UserDto bookerArg, ItemDto itemArg) {
        id = idArg;
        startBookingDate = startBookingDateArg;
        endBookingDate = endBookingDateArg;
        status = statusArg;
        bookerDto = bookerArg;
        itemDto = itemArg;
    }

    @Override
    public String toString() {
        String result = "BookingDto{" + "id=" + id + ", startBookingDate=" + startBookingDate +
                ", endBookingDate=" + endBookingDate + ", status='" + status + '\'';

        if (bookerDto != null) {
            result = result + ", " + bookerDto.toString();
        }
        if (itemDto != null) {
            result = result + ", " + itemDto.toString();
        }
        return result + "}" + '\n';
    }
}

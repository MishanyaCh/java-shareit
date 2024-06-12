package ru.practicum.shareit.booking.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingDtoWithoutItemField {
    private Integer id;
    private Integer bookerId;
    private LocalDateTime start;
    private LocalDateTime end;

    public BookingDtoWithoutItemField(Integer idArg, Integer bookerIdArg, LocalDateTime startArg,
                                      LocalDateTime endArg) {
        id = idArg;
        bookerId = bookerIdArg;
        start = startArg;
        end = endArg;
    }

    @Override
    public String toString() {
        return "BookingDtoWithoutItemField{" + "id=" + id + ", bookerId=" + bookerId + ", start=" + start +
                ", end=" + end + "}" + '\n';
    }
}

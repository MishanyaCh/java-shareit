package ru.practicum.shareit.booking.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class BookingCreateRequestDto {
    @NotNull(message = "Поле с идентификатором бронируемой вещи должно присутствовать!")
    private Integer itemId;

    @NotNull(message = "Поле c датой начала бронирования должно присутствовать!")
    @FutureOrPresent(message = "Дата начала бронирования не может быть раньше чем текущий день")
    private LocalDateTime start;

    @NotNull(message = "Поле с датой окончания бронирования должно присутствовать!")
    @Future(message = "Дата окончания бронирования не может быть прошедшим днем!")
    private LocalDateTime end;

    public BookingCreateRequestDto(Integer itemIdArg, LocalDateTime startArg, LocalDateTime endArg) {
        itemId = itemIdArg;
        start = startArg;
        end = endArg;
    }

    @Override
    public String toString() {
        return "BookingCreateRequestDto{" + "itemId=" + itemId + ", start=" + start + ", end=" + end + "}" + '\n';
    }
}

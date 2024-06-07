package ru.practicum.shareit.booking;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingResponseDtoTest {
    @Autowired
    private JacksonTester<BookingResponseDto> bookingResponseDtoJacksonTester;

    @Test
    @SneakyThrows
    void bookingResponseDtoSerialize() {
        LocalDateTime start = LocalDateTime.now().withNano(0);
        LocalDateTime end = LocalDateTime.now().withNano(0).plusDays(1);
        ItemDto itemDto = new ItemDto(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, null);
        UserDto userDto = new UserDto(1, "User", "User@mail.ru");
        BookingResponseDto bookingResponseDto = new BookingResponseDto(1, start, end, "WAITING",
                userDto, itemDto);

        JsonContent<BookingResponseDto> result = bookingResponseDtoJacksonTester.write(bookingResponseDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(start.toString());
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(end.toString());
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
        assertThat(result).extractingJsonPathValue("$.booker").isNotNull();
        assertThat(result).extractingJsonPathValue("$.item").isNotNull();
    }
}
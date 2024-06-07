package ru.practicum.shareit.item;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDtoWithoutItemField;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComment;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoWithBookingAndCommentTest {
    @Autowired
    private JacksonTester<ItemDtoWithBookingAndComment> dtoWithBookingAndCommentJacksonTester;
    private CommentDto commentDto;
    private BookingDtoWithoutItemField lastBooking;
    private BookingDtoWithoutItemField nextBooking;

    @BeforeEach
    void setup() {
        lastBooking = new BookingDtoWithoutItemField(1,1,
                LocalDateTime.now().withNano(0).minusDays(1),
                LocalDateTime.now().withNano(0).minusHours(10));
        nextBooking = new BookingDtoWithoutItemField(2, 3,
                LocalDateTime.now().withNano(0).plusHours(10),
                LocalDateTime.now().withNano(0).plusDays(1));
        commentDto = new CommentDto(1, "Отличный нивелир!!!", "User", LocalDateTime.MIN);
    }

    @Test
    @SneakyThrows
    void itemDtoWithBookingAndCommentSerialize_whenItemNotBooked() {
        ItemDtoWithBookingAndComment itemDtoWithBookingAndComment = new ItemDtoWithBookingAndComment(
                1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro", true,
                null, null, Collections.emptyList(), null);

        JsonContent<ItemDtoWithBookingAndComment> result = dtoWithBookingAndCommentJacksonTester
                .write(itemDtoWithBookingAndComment);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Лазерный нивелир");
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Лазерный нивелир EX600-Pro");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).doesNotHaveJsonPath("$.ownerId");
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isNull();
        assertThat(result).extractingJsonPathValue("$.lastBooking").isNull();
        assertThat(result).extractingJsonPathValue("$.nextBooking").isNull();
        assertThat(result).extractingJsonPathArrayValue("$.comments").isEmpty();
    }

    @Test
    @SneakyThrows
    void itemDtoWithBookingAndCommentSerialize_whenItemHasBookedAndHasComment() {
        ItemDtoWithBookingAndComment itemDtoWithBookingAndComment = new ItemDtoWithBookingAndComment(
                1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro", true,
                lastBooking, nextBooking, List.of(commentDto), null);

        JsonContent<ItemDtoWithBookingAndComment> result = dtoWithBookingAndCommentJacksonTester
                .write(itemDtoWithBookingAndComment);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Лазерный нивелир");
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Лазерный нивелир EX600-Pro");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).doesNotHaveJsonPath("$.ownerId");
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isNull();
        assertThat(result).extractingJsonPathValue("$.lastBooking").isNotNull();
        assertThat(result).extractingJsonPathValue("$.nextBooking").isNotNull();
        assertThat(result).extractingJsonPathArrayValue("$.comments").isNotEmpty();
    }
}
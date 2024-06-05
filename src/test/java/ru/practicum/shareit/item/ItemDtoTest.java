package ru.practicum.shareit.item;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoTest {
    @Autowired
    private JacksonTester<ItemDto> itemDtoJacksonTester;

    @Test
    @SneakyThrows
    void itemDtoSerialize_whenItemAddedNotOnRequest() {
        ItemDto itemDto = new ItemDto(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, null);

        JsonContent<ItemDto> result = itemDtoJacksonTester.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Лазерный нивелир");
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Лазерный нивелир EX600-Pro");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).doesNotHaveJsonPath("$.ownerId");
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isNull();
    }

    @Test
    @SneakyThrows
    void itemDtoSerialize_whenItemAddedOnRequest() {
        ItemDto itemDto = new ItemDto(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, 1);

        JsonContent<ItemDto> result = itemDtoJacksonTester.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Лазерный нивелир");
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Лазерный нивелир EX600-Pro");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).doesNotHaveJsonPath("$.ownerId");
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(1);
    }
}
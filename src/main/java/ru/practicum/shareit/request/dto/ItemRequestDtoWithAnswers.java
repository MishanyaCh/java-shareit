package ru.practicum.shareit.request.dto;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ItemRequestDtoWithAnswers extends ItemRequestDto {
    private List<ItemDto> items;

    public ItemRequestDtoWithAnswers(
            Integer idArg, String descriptionArg, LocalDateTime createdArg, List<ItemDto> itemsArg) {
        super(idArg, descriptionArg, createdArg);
        items = itemsArg;
    }

    @Override
    public String toString() {
        return "ItemRequestDtoWithAnswers{" + "id=" + getId() + ", description='" + getDescription() + '\'' +
                ", created=" + getCreated() + ", answersList=" + items + "}";
    }
}

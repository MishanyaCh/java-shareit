package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ItemDto {
    private int id;

    @NotBlank
    private String name;

    @NotNull
    private String description;

    @NotNull
    @JsonProperty("available")
    private Boolean isAvailable; // статус: если true, то доступна для аренды, а если false - недоступна

    @JsonIgnore
    private int ownerId; // владелец вещи

    public ItemDto(int idArg, String nameArg, String descriptionArg, Boolean isAvailableArg) {
        id = idArg;
        name = nameArg;
        description = descriptionArg;
        isAvailable = isAvailableArg;
        ownerId = 0;
    }

    @Override
    public String toString() {
        return "ItemDto{" + "id=" + id + ", name='" + name + '\'' + ", description='" + description + '\'' +
                ", isAvailable=" + isAvailable + "}" + '\n';
    }
}

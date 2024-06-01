package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@Setter
public class ItemRequestDto {
    private Integer id;

    @NotBlank(message = "Описание желаемой вещи должно содержать хотя бы один непробельный символ!")
    private String description;
    private LocalDateTime created;

    public ItemRequestDto(Integer idArg, String descriptionArg, LocalDateTime createdArg) {
        id = idArg;
        description = descriptionArg;
        created = createdArg;
    }

    public String toString() {
        return "ItemRequestDto{" + "id=" + id + ", description='" + description + '\'' + ", created=" + created + "}";
    }
}

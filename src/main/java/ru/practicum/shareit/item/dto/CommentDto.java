package ru.practicum.shareit.item.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@Setter
public class CommentDto {
    private int id;

    @NotBlank(message = "Комментарий должен содержать хотя бы один непробельный символ!")
    private String text;
    private String authorName;
    private LocalDateTime created;

    public CommentDto(int idArg, String textArg, String authorNameArg, LocalDateTime createdArg) {
        id = idArg;
        text = textArg;
        authorName = authorNameArg;
        created = createdArg;
    }

    public String toString() {
        return "CommentDto{" + "id=" + id + ", text='" + text + '\'' + ", authorName='" + authorName + '\'' +
                ", created=" + created + "}" + '\n';
    }
}

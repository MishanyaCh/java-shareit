package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class CommentMapperImpl implements CommentMapper {
    @Override
    public CommentDto toCommentDto(Comment comment) {
        int id = comment.getId();
        String text = comment.getText();
        LocalDateTime created = comment.getCreationDate();
        String authorName = comment.getAuthor().getName();
        return new CommentDto(id, text, authorName, created);
    }

    @Override
    public Comment toComment(CommentDto commentDto) {
        return new Comment(commentDto.getId(), commentDto.getText(), commentDto.getCreated(),
                null, null);
    }

    @Override
    public List<CommentDto> toCommentDtoList(List<Comment> comments) {
        List<CommentDto> dtoList = new ArrayList<>();
        if (comments.isEmpty()) {
            return dtoList;
        }
        for (Comment comment : comments) {
            CommentDto commentDto = toCommentDto(comment);
            dtoList.add(commentDto);
        }
        return dtoList;
    }
}

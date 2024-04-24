package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentMapper {
    CommentDto toCommentDto(Comment comment);

    Comment toComment(CommentDto commentDto);

    List<CommentDto> toCommentDtoList(List<Comment> comments);
}

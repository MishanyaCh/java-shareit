package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface CommentMapper {
    CommentDto toCommentDto(Comment comment);

    Comment toComment(CommentDto commentDto, Item item, User author);

    List<CommentDto> toCommentDtoList(List<Comment> comments);
}

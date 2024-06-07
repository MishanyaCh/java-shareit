package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.CommentMapperImpl;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperImplTest {
    CommentMapper commentMapper = new CommentMapperImpl();
    private Item savedItem;
    private User savedUser;
    private CommentDto inputCommentDto;
    private Comment comment;
    private CommentDto commentDto;
    private LocalDateTime creationDate;

    @BeforeEach
    void setUp() {
        creationDate = LocalDateTime.now();
        savedItem = new Item(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, 2);
        savedUser = new User(3, "User", "User@mail.ru");
        inputCommentDto = new CommentDto(null, "Отличный нивелир!!", null, null);
        comment = new Comment(1, "Отличный нивелир!!", creationDate, savedItem, savedUser);
        commentDto = new CommentDto(1, "Отличный нивелир!!", "User", creationDate);
    }

    @Test
    void toCommentDto_whenInvoke_thenReturnCommentDto() {
        CommentDto excepted = new CommentDto(1, "Отличный нивелир!!", "User", creationDate);

        CommentDto result = commentMapper.toCommentDto(comment);

        assertNotNull(result);
        assertEquals(excepted.getId(), result.getId());
        assertEquals(excepted.getText(), result.getText());
        assertEquals(excepted.getAuthorName(), result.getAuthorName());
        assertEquals(excepted.getCreated(), result.getCreated());
    }

    @Test
    void toComment_whenInvoke_thenReturnComment() {
        Comment excepted = new Comment(0, "Отличный нивелир!!", LocalDateTime.now(), savedItem, savedUser);

        Comment result = commentMapper.toComment(inputCommentDto, savedItem, savedUser);

        assertNotNull(result);
        assertEquals(excepted.getId(), result.getId());
        assertEquals(excepted.getText(), result.getText());
        assertEquals(excepted.getCreationDate().withNano(0), result.getCreationDate().withNano(0));
        assertNotNull(result.getItem());
        assertNotNull(result.getAuthor());
    }

    @Test
    void toCommentDtoList_whenInvoke_thenReturnCommentDtoList() {
        List<CommentDto> expected = List.of(commentDto);

        List<CommentDto> result = commentMapper.toCommentDtoList(List.of(comment));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(expected.size(), result.size());
    }

    @Test
    void toCommentDtoList_whenCommentsListIsEmpty_thenReturnEmptyCommentDtoList() {
        List<CommentDto> result = commentMapper.toCommentDtoList(new ArrayList<>());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
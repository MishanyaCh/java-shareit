package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.item.Storage.CommentRepository;
import ru.practicum.shareit.item.Storage.ItemRepository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Sql(value = {"/schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CommentRepositoryTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        User user = new User(0, "User", "User@mail.ru");
        User savedUser = userRepository.save(user);
        Item item = new Item(0, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, 1);
        Item savedItem = itemRepository.save(item);
        Comment comment = new Comment(0, "Отличный нивелир!!",
                LocalDateTime.now().withNano(0), savedItem, savedUser);
        commentRepository.save(comment);
    }

    @Test
    void findCommentsByItemOwner() {
        List<Comment> result = commentRepository.findCommentsByItemOwner(1);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void findCommentsByItemOwner_whenUserNotItemOwner_thenReturnEmptyCommentsList() {
        List<Comment> result = commentRepository.findCommentsByItemOwner(2);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
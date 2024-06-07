package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;
import ru.practicum.shareit.util.Page;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Sql(value = {"/schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ItemRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @BeforeEach
    void setUp() {
        User user = new User(0, "User", "User@mail.ru");
        userRepository.save(user);
        ItemRequest itemRequest = new ItemRequest(0, "Нужен лазерный нивелир", user,
                LocalDateTime.now().withNano(0));
        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        Item item = new Item(0, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, 1);
        item.setRequest(savedRequest);
        itemRepository.save(item);
    }

    @Test
    void searchItems() {
        String text = "лаз";
        Pageable pageable = Page.getPage(0,3);
        List<Item> result = itemRepository.searchItems(text, pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void findItemsByRequesterId_whenInvoke_thenReturnItemsList() {
        List<Item> result = itemRepository.findItemsByRequesterId(1);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void findItemsByRequesterId_whenRequesterNotFound_thenReturnEmptyItemsList() {
        List<Item> result = itemRepository.findItemsByRequesterId(2);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findItemsByRequestIdIn_whenInvoke_thenReturnItemsList() {
        List<Item> result = itemRepository.findItemsByRequestIdIn(List.of(1));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
}
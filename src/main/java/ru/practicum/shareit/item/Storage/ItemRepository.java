package ru.practicum.shareit.item.Storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {
    @Query(value = "SELECT * FROM items " +
            "WHERE (LOWER(name) LIKE %:text% OR LOWER(description) LIKE %:text%) AND is_available = true",
            nativeQuery = true)
    List<Item> searchItems(String text);

    List<Item> findItemsByOwnerId(int ownerId);
}

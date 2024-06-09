package ru.practicum.shareit.item.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {
    @Query(value = "SELECT * FROM items " +
            "WHERE (LOWER(name) LIKE %:text% OR LOWER(description) LIKE %:text%) AND is_available = true",
            nativeQuery = true)
    List<Item> searchItems(String text, Pageable pageable);

    List<Item> findItemsByOwnerId(int ownerId);

    List<Item> findItemsByRequestId(int requestId);

    @Query(value = "SELECT item.id, item.name, item.description, item.is_available, item.owner_id, item.request_id, " +
            "item_req.id, " +
            "item_req.description, " +
            "item_req.creation_date " +
            "FROM (SELECT * FROM item_requests WHERE requester_id = :userId) AS item_req " +
            "INNER JOIN items AS item ON item_req.id = item.request_id " +
            "WHERE item.id IS NOT NULL", nativeQuery = true)
    List<Item> findItemsByRequesterId(int userId);

    @Query(value = "SELECT item.id, item.name, item.description, item.is_available, item.owner_id, item.request_id, " +
            "item_req.id, " +
            "item_req.description, " +
            "item_req.creation_date " +
            "FROM (SELECT * FROM item_requests WHERE requester_id IN :requestIds) AS item_req " +
            "INNER JOIN items AS item ON item_req.id = item.request_id " +
            "WHERE item.id IS NOT NULL", nativeQuery = true)
    List<Item> findItemsByRequestIdIn(List<Integer> requestIds);
}

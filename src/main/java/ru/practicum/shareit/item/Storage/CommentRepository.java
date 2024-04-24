package ru.practicum.shareit.item.Storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findCommentsByItemId(int itemId);

    @Query(value = "SELECT * FROM comments " +
            "WHERE item_id IN ( SELECT id FROM items " +
                                "WHERE owner_id = :ownerId)", nativeQuery = true)
    List<Comment> findCommentsByItemOwner(int ownerId);
}

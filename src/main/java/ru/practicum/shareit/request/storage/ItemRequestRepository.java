package ru.practicum.shareit.request.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Integer> {
    List<ItemRequest> findItemsRequestsByRequesterId(int requesterId, Sort sort);

    List<ItemRequest> findItemsRequestsByRequesterIdNot(int requesterId, Pageable pageable);
}

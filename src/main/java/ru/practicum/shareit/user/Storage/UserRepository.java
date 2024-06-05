package ru.practicum.shareit.user.Storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.user.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
}

package ru.practicum.shareit.user.Storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    User createUser(User user);
    
    User updateUser(User user);

    List<User> getUsers();

    User getUser(int id);

    void deleteUser(int id);
}

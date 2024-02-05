package ru.practicum.shareit.user.Storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Component
public class UserInMemoryStorageImpl implements UserStorage {
    private int id = 0;
    private final Map<Integer, User> users = new HashMap<>(); // хеш-таблица для хранения пользователей

    @Override
    public User createUser(User user) {
        int userId = generateId();
        user.setId(userId);
        users.put(userId, user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        int userId = user.getId();
        String updatedName = user.getName();
        String updatedEmail = user.getEmail();

        User savedUser = users.get(userId);// получаем сохраненного пользователя, данные которого нужно обновить
        if (updatedName != null) {
            savedUser.setName(updatedName);
        }
        if (updatedEmail != null) {
            savedUser.setEmail(updatedEmail);
        }
        return users.get(userId);
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUser(int id) {
        return users.get(id);
    }

    @Override
    public void deleteUser(int id) {
        users.remove(id);
    }

    private int generateId() {
        return ++id;
    }
}

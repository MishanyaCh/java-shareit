package ru.practicum.shareit.user.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class User {
    private int id;
    private String name; // имя или логин пользователя
    private String email;

    public User(int idArg, String nameArg, String emailArg) {
        id = idArg;
        name = nameArg;
        email = emailArg;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", login='" + name + '\'' + ", email='" + email + '\'' + "}" + '\n';
    }
}

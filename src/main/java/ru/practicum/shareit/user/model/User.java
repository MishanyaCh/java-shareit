package ru.practicum.shareit.user.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    private String name; // имя или логин пользователя

    @Column(name = "email", unique = true)
    private String email;

    public User() { // конструктор без параметров для работы hibernate
    }

    public User(int idArg, String nameArg, String emailArg) {
        id = idArg;
        name = nameArg;
        email = emailArg;
    }
}

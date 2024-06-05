package ru.practicum.shareit.item.model;

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
@Table(name = "items")
public class Item {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_available")
    private Boolean isAvailable; // статус: если true, то доступна для аренды, а если false - недоступна

    @Column(name = "owner_id")
    private int ownerId; // владелец вещи

    public Item() { // конструктор без параметров для работы hibernate
    }

    public Item(int idArg, String nameArg, String descriptionArg, Boolean isAvailableArg, int ownerIdArg) {
        id = idArg;
        name = nameArg;
        description = descriptionArg;
        isAvailable = isAvailableArg;
        ownerId = ownerIdArg;
    }
}

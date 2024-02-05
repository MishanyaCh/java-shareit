package ru.practicum.shareit.item.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class Item {
    private int id;
    private String name;
    private String description;
    private Boolean isAvailable; // статус: если true, то доступна для аренды, а если false - недоступна
    private int ownerId; // владелец вещи

    public Item(int idArg, String nameArg, String descriptionArg, Boolean isAvailableArg, int ownerIdArg) {
        id = idArg;
        name = nameArg;
        description = descriptionArg;
        isAvailable = isAvailableArg;
        ownerId = ownerIdArg;
    }

    @Override
    public String toString() {
        String result = "Item{" + "id=" + id + ", name='" + name + '\'';

        if (description != null) {
            result = result + ", description.length=" + description.length();
        } else {
            result = result + ", description.length='null'";
        }
        return result + ", isAvailable=" + isAvailable + ", ownerId=" + ownerId + "}" + '\n';
    }
}

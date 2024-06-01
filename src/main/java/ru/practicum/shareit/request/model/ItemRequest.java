package ru.practicum.shareit.request.model;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.user.model.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "item_requests")
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "description")
    private String description; // текст запроса, содержащий описание вещи

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private User requester; // пользователь, создавший запрос на добавление желаемой вещи

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    public ItemRequest() { // конструктор без параметров для работы hibernate
    }

    public ItemRequest(int idArg, String descriptionArg, User requesterArg, LocalDateTime creationDateArg) {
        id = idArg;
        description = descriptionArg;
        requester = requesterArg;
        creationDate = creationDateArg;
    }
}

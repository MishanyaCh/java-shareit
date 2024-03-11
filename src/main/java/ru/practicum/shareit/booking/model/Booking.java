package ru.practicum.shareit.booking.model;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Getter
@Setter
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item; // вещь, бронируемая пользователем

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booker_id")
    private User booker;// пользователь, бронирующий вещь

    @Column(name = "start_booking_date")
    private LocalDateTime startBookingDate; // начало бронирования

    @Column(name = "end_booking_date")
    private LocalDateTime endBookingDate; // окончание бронирования

    @Column(name = "booking_status")
    @Enumerated(EnumType.STRING)
    private Status status; // текущий статус бронирования

    public Booking() { // конструктор без параметров для работы hibernate
    }

    public Booking(int idArg, Item itemArg, User bookerArg, LocalDateTime startBookingDateArg,
                   LocalDateTime endBookingDateArg, Status statusArg) {
        id = idArg;
        item = itemArg;
        booker = bookerArg;
        startBookingDate = startBookingDateArg;
        endBookingDate = endBookingDateArg;
        status = statusArg;
    }
}

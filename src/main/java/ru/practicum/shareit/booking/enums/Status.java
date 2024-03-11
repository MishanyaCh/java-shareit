package ru.practicum.shareit.booking.enums;

public enum Status {
    WAITING, // ожидание одобрения нового бронирования
    APPROVED, // бронирование подтверждено владельцем вещи
    REJECTED, // бронирование отклонено владельцем вещи
    CANCELED // бронирование отменено создателем
}

package ru.practicum.shareit.booking.Storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findBookingsByBookerIdAndStatus(int bookerId, Status status, Pageable pageable);

    List<Booking> findBookingsByBookerIdAndEndBookingDateBefore(
            int bookerId, LocalDateTime currentDateTime, Pageable pageable);

    List<Booking> findBookingsByBookerIdAndStartBookingDateAfter(
            int bookerId, LocalDateTime currentDateTime, Pageable pageable);

    List<Booking> findBookingsByBookerIdAndStartBookingDateBeforeAndEndBookingDateAfter(
            int bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findBookingsByBookerId(int bookerId, Pageable pageable);

    List<Booking> findBookingsByItemOwnerIdAndStatus(int ownerId, Status status, Pageable pageable);

    List<Booking> findBookingsByItemOwnerIdAndEndBookingDateBefore(
            int ownerId, LocalDateTime currentDateTime, Pageable pageable);

    List<Booking> findBookingsByItemOwnerIdAndStartBookingDateAfter(
            int ownerId, LocalDateTime currentDateTime, Pageable pageable);

    List<Booking> findBookingsByItemOwnerIdAndStartBookingDateAndEndBookingDate(
            int ownerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findBookingsByItemOwnerId(int ownerId, Pageable pageable);

    List<Booking> findBookingsByItemIdAndStatusNot(int itemId, Status status, Sort sort);

    List<Booking> findBookingsByItemIdAndBookerIdAndStatus(int itemId, int bookerId, Status status);
}

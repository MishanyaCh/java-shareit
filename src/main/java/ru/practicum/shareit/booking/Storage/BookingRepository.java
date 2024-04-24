package ru.practicum.shareit.booking.Storage;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findBookingsByBookerIdAndStatus(int bookerId, Status status, Sort sort);

    List<Booking> findBookingsByBookerIdAndEndBookingDateBefore(int bookerId, LocalDateTime currentDateTime, Sort sort);

    List<Booking> findBookingsByBookerIdAndStartBookingDateAfter(int bookerId, LocalDateTime currentDateTime, Sort sort);

    List<Booking> findBookingsByBookerIdAndStartBookingDateBeforeAndEndBookingDateAfter(int bookerId,
                                                                                        LocalDateTime start,
                                                                                        LocalDateTime end,
                                                                                        Sort sort);
    List<Booking> findBookingsByBookerId(int bookerId, Sort sort);

    @Query(value = "SELECT * FROM bookings " +
            "WHERE item_id IN ( SELECT id FROM items " +
                                "WHERE owner_id = :ownerId) " +
            "AND booking_status = :status " +
            "ORDER BY start_booking_date DESC", nativeQuery = true)
    List<Booking> findBookingsByItemOwnerAndStatus(int ownerId, String status);

    @Query(value = "SELECT * FROM bookings " +
            "WHERE item_id IN ( SELECT id FROM items " +
                                "WHERE owner_id = :ownerId) " +
            "AND end_booking_date < :currentDateTime " +
            "ORDER BY start_booking_date DESC", nativeQuery = true)
    List<Booking> findBookingsByItemOwnerAndEndBookingDateBefore(int ownerId, LocalDateTime currentDateTime);

    @Query(value = "SELECT * FROM bookings " +
            "WHERE item_id IN ( SELECT id FROM items " +
                                "WHERE owner_id = :ownerId) " +
            "AND start_booking_date > :currentDateTime " +
            "ORDER BY start_booking_date DESC", nativeQuery = true)
    List<Booking> findBookingsByItemOwnerAndStartBookingDateAfter(int ownerId, LocalDateTime currentDateTime);

    @Query(value = "SELECT * FROM bookings " +
            "WHERE item_id IN ( SELECT id FROM items " +
                                "WHERE owner_id = :ownerId) " +
            "AND start_booking_date < :currentDateTime AND end_booking_date > :currentDateTime " +
            "ORDER BY start_booking_date DESC", nativeQuery = true)
    List<Booking> findBookingsByItemOwnerAndStartBookingDateAndEndBookingDate(int ownerId,
                                                                              LocalDateTime currentDateTime);

    @Query(value = "SELECT * FROM bookings " +
            "WHERE item_id IN ( SELECT id FROM items " +
                                "WHERE owner_id = :ownerId) " +
            "ORDER BY start_booking_date DESC", nativeQuery = true)
    List<Booking> findBookingsByItemOwner(int ownerId);

    List<Booking> findBookingsByItemIdAndStatusNot(int itemId, Status status, Sort sort);

    List<Booking> findBookingsByItemIdAndBookerIdAndStatus(int itemId, int bookerId, Status status);
}

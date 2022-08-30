package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBooker_IdOrderByStartDesc(long userId);

    List<Booking> searchBookingByItem_Owner_Id(long id);

    List<Booking> searchBookingByBooker_IdAndItem_IdAndEndIsBefore(long id, long itemId, LocalDateTime time);

    List<Booking> searchBookingByItem_Owner_IdAndStartIsAfterOrderByStartDesc(long id, LocalDateTime time);

    List<Booking> findByBooker_IdAndStartAfterOrderByStartDesc(long userId, LocalDateTime time);

    List<Booking> findBookingsByItem_IdAndEndIsBeforeOrderByEndDesc(long id, LocalDateTime time);

    List<Booking> findBookingsByItem_IdAndStartIsAfterOrderByStartDesc(long id, LocalDateTime time);

    List<Booking> findBookingsByBooker_IdAndStatusOrderByStartDesc(long userId, Status status);

    List<Booking> findBookingsByItem_Owner_IdOrderByStartDesc(long id);

    @Query("select b " +
            "from Booking b left join User as us on b.booker.id = us.id " +
            "where us.id = ?1 " +
            "and ?2 between b.start and b.end " +
            "order by b.start DESC")
    List<Booking> findCurrentBookingsByBooker_IdOrderByStartDesc(long userId, LocalDateTime time);

    @Query("select b " +
            "from Booking b left join Item as i on b.item.id = i.id " +
            "left join User as us on i.owner.id = us.id " +
            "where us.id = ?1 " +
            "and ?2 between b.start and b.end " +
            "order by b.start DESC")
    List<Booking> findCurrentBookingsByItem_Owner_IdOrderByStartDesc(long userId, LocalDateTime time);

    List<Booking> findBookingsByBooker_IdAndEndIsBeforeOrderByStartDesc(long userId, LocalDateTime time);

    List<Booking> findBookingsByItem_Owner_IdAndEndIsBeforeOrderByStartDesc(long userId, LocalDateTime time);
}

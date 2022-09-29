package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBooker_Id(long userId, Pageable pageable);

    List<Booking> searchBookingByItem_Owner_Id(long id, Pageable pageable);

    List<Booking> searchBookingByBooker_IdAndItem_IdAndEndIsBeforeAndStatus(long id, long itemId,
                                                                   LocalDateTime time, Status status);

    List<Booking> searchBookingByItem_Owner_IdAndStartIsAfter(long id,
                                                              LocalDateTime time,
                                                              Pageable pageable);

    List<Booking> findByBooker_IdAndStartAfter(long userId, LocalDateTime time,
                                               Pageable pageable);

    List<Booking> findBookingsByItem_IdAndEndIsBeforeOrderByEndDesc(long id, LocalDateTime time);

    List<Booking> findBookingsByItem_IdAndStartIsAfterOrderByStartDesc(long id, LocalDateTime time);

    List<Booking> findBookingsByBooker_IdAndStatus(long userId, Status status,
                                                   Pageable pageable);

    List<Booking> findBookingsByItem_Owner_Id(long id, Pageable pageable);

    @Query("select b " +
            "from Booking b left join User as us on b.booker.id = us.id " +
            "where us.id = ?1 " +
            "and ?2 between b.start and b.end " +
            "order by b.start DESC")
    List<Booking> findCurrentBookingsByBooker_Id(long userId, LocalDateTime time,
                                                 Pageable pageable);

    @Query("select b " +
            "from Booking b left join Item as i on b.item.id = i.id " +
            "left join User as us on i.owner.id = us.id " +
            "where us.id = ?1 " +
            "and ?2 between b.start and b.end " +
            "order by b.start DESC")
    List<Booking> findCurrentBookingsByItem_Owner_Id(long userId, LocalDateTime time,
                                                     Pageable pageable);

    List<Booking> findBookingsByBooker_IdAndEndIsBefore(long userId, LocalDateTime time,
                                                        Pageable pageable);

    List<Booking> findBookingsByItem_Owner_IdAndEndIsBefore(long userId,
                                                            LocalDateTime time,
                                                            Pageable pageable);
}

package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    ItemRepository itemRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    BookingRepository bookingRepository;

    private Item item;
    private User owner;
    private User booker;
    private Booking booking;

    @BeforeEach
    void beforeEach() {
        owner = userRepository.save(new User(1L, "user1", "user1@mail.ru"));
        booker = userRepository.save(new User(2L, "user2", "user2@mail.ru"));
        item = itemRepository.save(new Item(1L, "item", "description",
                true, owner, null));
        booking = bookingRepository.save(new Booking(1L, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(2),
                item, booker, Status.APPROVED));
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        bookingRepository.deleteAll();
    }

    @Test
    void findByBooker_Id() {
        final List<Booking> bookings = bookingRepository.findByBooker_Id(booker.getId(),
                Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking, bookings.get(0));
    }

    @Test
    void searchBookingByItem_Owner_Id() {
        final List<Booking> bookings = bookingRepository.searchBookingByItem_Owner_Id(owner.getId(),
                Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking, bookings.get(0));
    }

    @Test
    void searchBookingByBooker_IdAndItem_IdAndEndIsBeforeAndStatus() {
        final List<Booking> bookings = bookingRepository
                .searchBookingByBooker_IdAndItem_IdAndEndIsBeforeAndStatus(booker.getId(),
                        item.getId(),
                        LocalDateTime.now(),
                        Status.APPROVED);
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking, bookings.get(0));
    }

    @Test
    void searchBookingByItem_Owner_IdAndStartIsAfter() {
        List<Booking> bookings = bookingRepository
                .searchBookingByItem_Owner_IdAndStartIsAfter(owner.getId(),
                        LocalDateTime.now(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(0, bookings.size());

        final Booking booking1 = bookingRepository.save(new Booking(2L, LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(10),
                item, booker, Status.APPROVED));
        bookings = bookingRepository
                .searchBookingByItem_Owner_IdAndStartIsAfter(owner.getId(),
                        LocalDateTime.now(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking1, bookings.get(0));
    }

    @Test
    void findByBooker_IdAndStartAfter() {
        List<Booking> bookings = bookingRepository
                .findByBooker_IdAndStartAfter(booker.getId(),
                        LocalDateTime.now(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(0, bookings.size());

        final Booking booking1 = bookingRepository.save(new Booking(2L,
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(10),
                item, booker, Status.APPROVED));
        bookings = bookingRepository
                .findByBooker_IdAndStartAfter(booker.getId(),
                        LocalDateTime.now(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking1, bookings.get(0));
    }

    @Test
    void findBookingsByItem_IdAndEndIsBeforeOrderByEndDesc() {
        final List<Booking> bookings = bookingRepository
                .findBookingsByItem_IdAndEndIsBeforeOrderByEndDesc(item.getId(),
                        LocalDateTime.now());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking, bookings.get(0));
    }

    @Test
    void findBookingsByItem_IdAndStartIsAfterOrderByStartDesc() {
        List<Booking> bookings = bookingRepository
                .findBookingsByItem_IdAndStartIsAfterOrderByStartDesc(item.getId(),
                        LocalDateTime.now());
        assertNotNull(bookings);
        assertEquals(0, bookings.size());

        final Booking booking1 = bookingRepository.save(new Booking(2L,
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(10),
                item, booker, Status.APPROVED));
        bookings = bookingRepository
                .findBookingsByItem_IdAndStartIsAfterOrderByStartDesc(item.getId(),
                        LocalDateTime.now());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking1, bookings.get(0));
    }

    @Test
    void findBookingsByBooker_IdAndStatus() {
        final List<Booking> bookings = bookingRepository
                .findBookingsByBooker_IdAndStatus(booker.getId(),
                        Status.APPROVED,
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking, bookings.get(0));
    }

    @Test
    void findBookingsByItem_Owner_Id() {
        final List<Booking> bookings = bookingRepository
                .findBookingsByItem_Owner_Id(item.getOwner().getId(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking, bookings.get(0));
    }

    @Test
    void findCurrentBookingsByBooker_Id() {
        List<Booking> bookings = bookingRepository
                .findCurrentBookingsByBooker_Id(booker.getId(),
                        LocalDateTime.now(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(0, bookings.size());

        final Booking booking1 = bookingRepository.save(new Booking(2L,
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().plusDays(10),
                item, booker, Status.APPROVED));
        bookings = bookingRepository
                .findCurrentBookingsByBooker_Id(booker.getId(),
                        LocalDateTime.now(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking1, bookings.get(0));
    }

    @Test
    void findCurrentBookingsByItem_Owner_Id() {
        List<Booking> bookings = bookingRepository
                .findCurrentBookingsByItem_Owner_Id(item.getOwner().getId(),
                        LocalDateTime.now(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(0, bookings.size());

        final Booking booking1 = bookingRepository.save(new Booking(2L,
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().plusDays(10),
                item, booker, Status.APPROVED));
        bookings = bookingRepository
                .findCurrentBookingsByItem_Owner_Id(item.getOwner().getId(),
                        LocalDateTime.now(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking1, bookings.get(0));
    }

    @Test
    void findBookingsByBooker_IdAndEndIsBefore() {
        final List<Booking> bookings = bookingRepository
                .findBookingsByBooker_IdAndEndIsBefore(booker.getId(),
                        LocalDateTime.now(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking, bookings.get(0));
    }

    @Test
    void findBookingsByItem_Owner_IdAndEndIsBefore() {
        final List<Booking> bookings = bookingRepository
                .findBookingsByItem_Owner_IdAndEndIsBefore(item.getOwner().getId(),
                        LocalDateTime.now(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking, bookings.get(0));
    }
}
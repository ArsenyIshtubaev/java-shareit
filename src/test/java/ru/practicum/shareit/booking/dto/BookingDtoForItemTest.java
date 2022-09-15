package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static ru.practicum.shareit.booking.enums.Status.APPROVED;

class BookingDtoForItemTest {

    private final BookingDtoForItem bookingDtoForItem = new BookingDtoForItem(1L, 2L);
    private Booking booking;
    private final BookingMapper mapper = new BookingMapper();

    private Booking createBooking() {
        User owner = new User(1L, "user1", "user1@mail.ru");
        User booker = new User(2L, "user2", "user2@mail.ru");
        Item item = new Item(1L, "item", "description",
                true, owner, null);
        LocalDateTime start = LocalDateTime.parse("2022-09-10T10:42");
        LocalDateTime end = LocalDateTime.parse("2022-09-12T10:42");
        booking = new Booking(1L, start,
                end, item, booker, APPROVED);
        return booking;
    }

    @Test
    void toBookingDtoForItem(){
        booking = createBooking();
        BookingDtoForItem bookingDtoForItem1 = mapper.toBookingDtoForItem(booking);
        assertNotNull(bookingDtoForItem1);
        assertEquals(bookingDtoForItem1.getBookerId(), 2L);
    }

    @Test
    void getId() {
        Long id = bookingDtoForItem.getId();
        assertEquals(id, 1);
    }

    @Test
    void setId() {
        bookingDtoForItem.setId(5L);
        assertEquals(bookingDtoForItem.getId(), 5);
    }
    @Test
    void getBookerId(){
        Long bookerId = bookingDtoForItem.getBookerId();
        assertEquals(bookerId, 2);
    }

    @Test
    void setBookerId(){
        bookingDtoForItem.setBookerId(5L);
        assertEquals(bookingDtoForItem.getBookerId(), 5);
    }
}
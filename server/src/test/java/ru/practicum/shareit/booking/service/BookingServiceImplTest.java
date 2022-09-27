package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.exception.StorageException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.booking.enums.Status.*;

class BookingServiceImplTest {

    private BookingService bookingService;
    private BookingRepository bookingRepository;
    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private BookingMapper bookingMapper;
    private Booking booking;

    @BeforeEach
    void beforeEach() {
        itemRepository = mock(ItemRepository.class);
        userRepository = mock(UserRepository.class);
        bookingRepository = mock(BookingRepository.class);
        bookingMapper = new BookingMapper();
        bookingService = new BookingServiceImpl(bookingRepository, itemRepository,
                userRepository, bookingMapper);
        booking = createBooking();
    }

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
    void findById() {
        Long bookingId = booking.getId();
        long incorrectId = (long) (Math.random() * 100) + bookingId + 3;
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.findById(incorrectId))
                .thenThrow(new StorageException("Бронирования с Id = " + incorrectId + " нет в БД"));
        BookingDto bookingDto = bookingService.findById(bookingId, booking.getBooker().getId());
        assertNotNull(bookingDto);
        assertEquals("item", bookingDto.getItem().getName());
        Throwable thrown = assertThrows(StorageException.class,
                () -> bookingService.findById(incorrectId, booking.getBooker().getId()));
        assertNotNull(thrown.getMessage());
        verify(bookingRepository, times(1)).findById(bookingId);
        long incorrectUserId = 10L;
        Throwable thrown2 = assertThrows(StorageException.class,
                () -> bookingService.findById(bookingId,
                        incorrectUserId));
        assertNotNull(thrown2.getMessage());
    }

    @Test
    void findAll() {
        when(userRepository.findById(booking.getBooker().getId()))
                .thenReturn(Optional.of(booking.getBooker()));
        when(bookingRepository.findByBooker_Id(booking.getBooker().getId(),
                PageRequest.of(0, 20, Sort.by("start").descending())))
                .thenReturn(Collections.singletonList(booking));
        final List<BookingDto> bookingDtos = bookingService
                .findAll(booking.getBooker().getId(), "ALL", 0, 20);
        assertNotNull(bookingDtos);
        assertEquals(1, bookingDtos.size());
        assertEquals(booking.getItem().getName(), bookingDtos.get(0).getItem().getName());
        verify(bookingRepository, times(1))
                .findByBooker_Id(booking.getBooker().getId(),
                        PageRequest.of(0, 20, Sort.by("start").descending()));

        booking.setStatus(WAITING);
        when(bookingRepository.findBookingsByBooker_IdAndStatus(booking.getBooker().getId(),
                WAITING,
                PageRequest.of(0, 20, Sort.by("start").descending())))
                .thenReturn(Collections.singletonList(booking));
        List<BookingDto> bookings2 = bookingService
                .findAll(booking.getBooker().getId(),
                        "WAITING", 0, 20);
        assertNotNull(bookings2);
        assertEquals(1, bookings2.size());
        assertEquals(booking.getStatus(), bookings2.get(0).getStatus());
        verify(bookingRepository, times(1))
                .findBookingsByBooker_IdAndStatus(booking.getBooker().getId(),
                        WAITING,
                        PageRequest.of(0, 20, Sort.by("start").descending()));

        booking.setStatus(REJECTED);
        when(bookingRepository.findBookingsByBooker_IdAndStatus(booking.getBooker().getId(),
                REJECTED,
                PageRequest.of(0, 20, Sort.by("start").descending())))
                .thenReturn(Collections.singletonList(booking));
        List<BookingDto> bookings3 = bookingService
                .findAll(booking.getBooker().getId(),
                        "REJECTED", 0, 20);
        assertNotNull(bookings3);
        assertEquals(1, bookings3.size());
        assertEquals(booking.getStatus(), bookings3.get(0).getStatus());
        verify(bookingRepository, times(1))
                .findBookingsByBooker_IdAndStatus(booking.getBooker().getId(),
                        REJECTED,
                        PageRequest.of(0, 20, Sort.by("start").descending()));
        String incorrectState = "error";
        Throwable thrown = assertThrows(BookingException.class,
                () -> bookingService.findAll(booking.getBooker().getId(),
                        incorrectState, 0, 20));
        assertNotNull(thrown.getMessage());
    }

    @Test
    void save() {
        booking.setStatus(WAITING);
        when(userRepository.findById(booking.getBooker().getId()))
                .thenReturn(Optional.of(booking.getBooker()));
        when(itemRepository.findById(booking.getItem().getId()))
                .thenReturn(Optional.of(booking.getItem()));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);
        BookingDto bookingDto = bookingService.save(bookingMapper.toBookingDtoSimple(booking),
                booking.getBooker().getId());
        assertNotNull(bookingDto);
        assertEquals("item", bookingDto.getItem().getName());
        assertEquals("user2", bookingDto.getBooker().getName());
        assertEquals(booking.getId(), bookingDto.getId());
        verify(bookingRepository, times(1)).save(booking);

       LocalDateTime errorEnd = booking.getEnd().minusDays(30);
       booking.setEnd(errorEnd);
        Throwable thrown = assertThrows(BookingException.class,
                () -> bookingService.save(bookingMapper.toBookingDtoSimple(booking),
                        booking.getBooker().getId()));
        assertNotNull(thrown.getMessage());
    }

    @Test
    void update() {
        Booking booking2 = createBooking();
        long bookingId = booking.getId();
        booking2.setStatus(REJECTED);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking2);
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        BookingDto bookingDto = bookingService.update(bookingId,
                bookingMapper.toBookingDto(booking2));
        assertNotNull(bookingDto);
        assertEquals(REJECTED, bookingDto.getStatus());
        assertEquals(booking.getId(), bookingDto.getId());
        verify(bookingRepository, times(1)).save(booking2);
    }

    @Test
    void deleteById() {
        bookingService.deleteById(booking.getId());
        verify(bookingRepository, times(1)).deleteById(booking.getId());
    }

    @Test
    void approve() {
        long bookingId = booking.getId();
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        Throwable thrown = assertThrows(BookingException.class,
                () -> bookingService.approve(booking.getItem().getOwner().getId(),
                        bookingId, true));
        assertNotNull(thrown.getMessage());
        verify(bookingRepository, times(1)).findById(bookingId);
        Throwable thrown2 = assertThrows(BookingException.class,
                () -> bookingService.approve(booking.getItem().getOwner().getId(),
                        bookingId, null));
        assertNotNull(thrown2.getMessage());
    }

    @Test
    void findAllByItemOwnerId() {
        BookingDto bookingDto = bookingMapper.toBookingDto(booking);
        when(userRepository.findById(booking.getItem().getOwner().getId()))
                .thenReturn(Optional.of(booking.getBooker()));
        when(bookingRepository.searchBookingByItem_Owner_Id(booking.getItem().getOwner().getId(),
                PageRequest.of(0, 20, Sort.by("start").descending())))
                .thenReturn(Collections.singletonList(booking));
        List<BookingDto> bookings = bookingService
                .findAllByItemOwnerId(booking.getItem().getOwner().getId(),
                        "ALL", 0, 20);
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(bookingDto, bookings.get(0));
        verify(bookingRepository, times(1))
                .searchBookingByItem_Owner_Id(booking.getItem().getOwner().getId(),
                        PageRequest.of(0, 20, Sort.by("start").descending()));

        booking.setStatus(WAITING);
        when(bookingRepository.findBookingsByItem_Owner_Id(booking.getItem().getOwner().getId(),
                PageRequest.of(0, 20, Sort.by("start").descending())))
                .thenReturn(Collections.singletonList(booking));
        List<BookingDto> bookings2 = bookingService
                .findAllByItemOwnerId(booking.getItem().getOwner().getId(),
                        "WAITING", 0, 20);
        assertNotNull(bookings2);
        assertEquals(1, bookings2.size());
        assertEquals(booking.getStatus(), bookings2.get(0).getStatus());
        verify(bookingRepository, times(1))
                .findBookingsByItem_Owner_Id(booking.getItem().getOwner().getId(),
                        PageRequest.of(0, 20, Sort.by("start").descending()));

        booking.setStatus(REJECTED);
        when(bookingRepository.findBookingsByItem_Owner_Id(booking.getItem().getOwner().getId(),
                PageRequest.of(0, 20, Sort.by("start").descending())))
                .thenReturn(Collections.singletonList(booking));
        List<BookingDto> bookings3 = bookingService
                .findAllByItemOwnerId(booking.getItem().getOwner().getId(),
                        "REJECTED", 0, 20);
        assertNotNull(bookings3);
        assertEquals(1, bookings3.size());
        assertEquals(booking.getStatus(), bookings3.get(0).getStatus());
        verify(bookingRepository, times(2))
                .findBookingsByItem_Owner_Id(booking.getItem().getOwner().getId(),
                        PageRequest.of(0, 20, Sort.by("start").descending()));

        String incorrectState = "error";
        Throwable thrown = assertThrows(BookingException.class,
                () -> bookingService.findAllByItemOwnerId(booking.getItem().getOwner().getId(),
                        incorrectState, 0, 20));
        assertNotNull(thrown.getMessage());
    }
}
package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoSimple;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.booking.enums.Status.APPROVED;
import static ru.practicum.shareit.booking.enums.Status.REJECTED;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc
class BookingControllerTest {

    @MockBean
    private BookingService bookingService;

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private final BookingMapper bookingMapper = new BookingMapper();
    private Booking booking;

    @BeforeEach
    void beforeEach() {
        mapper.registerModule(new JavaTimeModule());
        booking = createBooking();
    }

    private Booking createBooking() {
        User owner = new User(1L, "user1", "user1@mail.ru");
        User booker = new User(2L, "user2", "user2@mail.ru");
        Item item = new Item(1L, "item", "description",
                true, owner, null);
        LocalDateTime start = LocalDateTime.parse("2022-09-10T10:42");
        LocalDateTime end = LocalDateTime.parse("2022-09-12T10:42");
        booking = new Booking(1L, start, end, item, booker, APPROVED);
        return booking;
    }

    @Test
    void findAll() throws Exception {
        List<BookingDto> bookingDtos = new ArrayList<>();
        BookingDto bookingDto = bookingMapper.toBookingDto(booking);
        bookingDtos.add(bookingDto);
        when(bookingService.findAll(bookingDto.getBooker().getId(), "ALL", 0, 20))
                .thenReturn(bookingDtos);
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", bookingDto.getBooker().getId())
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\": 1," +
                        " \"item\": {\"id\": 1,\"name\": \"item\"," +
                        " \"description\": \"description\", \"available\": true," +
                        " \"owner\": {\"id\": 1,\"name\": \"user1\",\"email\": \"user1@mail.ru\"}," +
                        " \"itemRequest\": null}," +
                        " \"booker\": {\"id\": 2,\"name\": \"user2\",\"email\": \"user2@mail.ru\"}}]"));
        verify(bookingService, times(1))
                .findAll(bookingDto.getBooker().getId(), "ALL", 0, 20);
    }

    @Test
    void findAllByOwner() throws Exception {
        List<BookingDto> bookingDtos = new ArrayList<>();
        BookingDto bookingDto = bookingMapper.toBookingDto(booking);
        bookingDtos.add(bookingDto);
        when(bookingService.findAllByItemOwnerId(bookingDto.getItem().getOwner().getId(),
                "ALL", 0, 20))
                .thenReturn(bookingDtos);
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", bookingDto.getItem().getOwner().getId())
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\": 1," +
                        " \"item\": {\"id\": 1,\"name\": \"item\"," +
                        " \"description\": \"description\", \"available\": true," +
                        " \"owner\": {\"id\": 1,\"name\": \"user1\",\"email\": \"user1@mail.ru\"}," +
                        " \"itemRequest\": null}," +
                        " \"booker\": {\"id\": 2,\"name\": \"user2\",\"email\": \"user2@mail.ru\"}}]"));
        verify(bookingService, times(1))
                .findAllByItemOwnerId(bookingDto.getItem().getOwner().getId(), "ALL", 0, 20);
    }

    @Test
    void create() throws Exception {
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        BookingDtoSimple bookingDtoSimple = bookingMapper.toBookingDtoSimple(booking);
        BookingDto bookingDto = bookingMapper.toBookingDto(booking);
        when(bookingService.save(bookingDtoSimple, 2)).thenReturn(bookingDto);
        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoSimple))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1," +
                        " \"item\": {\"id\": 1,\"name\": \"item\"," +
                        " \"description\": \"description\", \"available\": true," +
                        " \"owner\": {\"id\": 1,\"name\": \"user1\",\"email\": \"user1@mail.ru\"}," +
                        " \"itemRequest\": null}," +
                        " \"booker\": {\"id\": 2,\"name\": \"user2\",\"email\": \"user2@mail.ru\"}}"));
        verify(bookingService, times(1))
                .save(bookingDtoSimple, booking.getBooker().getId());
    }

    @Test
    void approve() throws Exception {
        booking.setStatus(REJECTED);
        BookingDto bookingDto = bookingMapper.toBookingDto(booking);
        bookingDto.setStatus(APPROVED);
        when(bookingService.approve(booking.getItem().getOwner().getId(),
                booking.getId(), true)).thenReturn(bookingDto);
        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(content().json("{\"id\": 1," +
                        " \"item\": {\"id\": 1,\"name\": \"item\"," +
                        " \"description\": \"description\", \"available\": true," +
                        " \"owner\": {\"id\": 1,\"name\": \"user1\",\"email\": \"user1@mail.ru\"}," +
                        " \"itemRequest\": null}," +
                        " \"booker\": {\"id\": 2,\"name\": \"user2\",\"email\": \"user2@mail.ru\"}}"));
        verify(bookingService, times(1))
                .approve(booking.getItem().getOwner().getId(),
                        booking.getId(), true);
    }

    @Test
    void findById() throws Exception {
        BookingDto bookingDto = bookingMapper.toBookingDto(booking);
        when(bookingService.findById(booking.getId(), bookingDto.getBooker().getId())).thenReturn(bookingDto);
        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", bookingDto.getBooker().getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1," +
                        " \"item\": {\"id\": 1,\"name\": \"item\"," +
                        " \"description\": \"description\", \"available\": true," +
                        " \"owner\": {\"id\": 1,\"name\": \"user1\",\"email\": \"user1@mail.ru\"}," +
                        " \"itemRequest\": null}," +
                        " \"booker\": {\"id\": 2,\"name\": \"user2\",\"email\": \"user2@mail.ru\"}}"));
        verify(bookingService, times(1)).findById(booking.getId(), bookingDto.getBooker().getId());
    }

    @Test
    void deleteById() throws Exception {
        mockMvc.perform(delete("/bookings/1"))
                .andExpect(status().isOk());
        verify(bookingService, times(1)).deleteById(booking.getId());
    }
}
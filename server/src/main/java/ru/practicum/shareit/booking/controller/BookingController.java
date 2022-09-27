package ru.practicum.shareit.booking.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoSimple;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

/**
 * // TODO .
 */
@RestController
@Slf4j
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<BookingDto> findAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @RequestParam(defaultValue = "ALL") String state,
                                    @RequestParam(defaultValue = "0") int from,
                                    @RequestParam(defaultValue = "20") int size) {
        return bookingService.findAll(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> findAllByOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @RequestParam(defaultValue = "ALL") String state,
                                           @RequestParam(defaultValue = "0") int from,
                                           @RequestParam(defaultValue = "20") int size) {
        return bookingService.findAllByItemOwnerId(userId, state, from, size);
    }

    @PostMapping
    public BookingDto create(@RequestBody BookingDtoSimple bookingDtoSimple,
                             @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Получен запрос к эндпоинту: '{} {}', Бронирование: ItemId: {}", "POST", "/bookings",
                bookingDtoSimple.getItemId());
        return bookingService.save(bookingDtoSimple, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader("X-Sharer-User-Id") long userId,
                              @PathVariable long bookingId, @RequestParam Boolean approved) {
        log.info("Получен запрос к эндпоинту: '{} {}', Подтверждение бронирование: ID: {}", "PATCH", "/bookings",
                bookingId);
        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/{id}")
    public BookingDto findById(@PathVariable long id, @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("GET booking id={}", id);
        return bookingService.findById(id, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable long id) {
        bookingService.deleteById(id);
    }

}

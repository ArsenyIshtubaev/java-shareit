package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoSimple;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.exception.ItemException;
import ru.practicum.shareit.exception.StorageException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper mapper;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, ItemRepository itemRepository,
                              UserRepository userRepository, BookingMapper mapper) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public BookingDto findById(long bookingId, long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new StorageException("Бронирования с Id = " + bookingId + " нет в БД"));
        if (booking.getBooker().getId() != userId
                && booking.getItem().getOwner().getId() != userId) {
            throw new StorageException("Incorrect userId");
        }
        return mapper.toBookingDto(booking);

    }

    @Override
    public List<BookingDto> findAll(long userId, String state) {
        userRepository.findById(userId).orElseThrow(() -> new StorageException("Incorrect userId"));
        try {
            switch (Status.valueOf(state)) {
                case ALL:
                    return bookingRepository.findByBooker_IdOrderByStartDesc(userId)
                            .stream().map(mapper::toBookingDto).collect(Collectors.toList());
                case CURRENT:
                    return bookingRepository.findCurrentBookingsByBooker_IdOrderByStartDesc(userId,
                                    LocalDateTime.now()).stream().map(mapper::toBookingDto)
                            .collect(Collectors.toList());
                case PAST:
                    return bookingRepository.findBookingsByBooker_IdAndEndIsBeforeOrderByStartDesc(userId,
                                    LocalDateTime.now()).stream().map(mapper::toBookingDto)
                            .collect(Collectors.toList());
                case FUTURE:
                    return bookingRepository.findByBooker_IdAndStartAfterOrderByStartDesc(userId,
                                    LocalDateTime.now())
                            .stream()
                            .map(mapper::toBookingDto).collect(Collectors.toList());
                case WAITING:
                    return bookingRepository.findBookingsByBooker_IdAndStatusOrderByStartDesc(userId,
                                    Status.WAITING)
                            .stream()
                            .map(mapper::toBookingDto).collect(Collectors.toList());
                case REJECTED:
                    return bookingRepository.findBookingsByBooker_IdAndStatusOrderByStartDesc(userId,
                                    Status.REJECTED)
                            .stream()
                            .map(mapper::toBookingDto).collect(Collectors.toList());
                default:
                    throw new BookingException("Unknown state: UNSUPPORTED_STATUS");
            }
        } catch (IllegalArgumentException e) {
            throw new BookingException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    public BookingDto save(BookingDtoSimple bookingDtoSimple, long userId) {
        if (bookingDtoSimple.getEnd().isBefore(bookingDtoSimple.getStart())) {
            throw new BookingException("Incorrect end time");
        }
        Booking booking = mapper.fromSimpleToBooking(bookingDtoSimple);
        booking.setBooker(userRepository.findById(userId).orElseThrow());
        Item item = itemRepository.findById(bookingDtoSimple.getItemId())
                .orElseThrow(() -> new StorageException("Вещи с Id = "
                        + bookingDtoSimple.getItemId() + " нет в базе данных"));
        if (!item.getAvailable()) {
            throw new ItemException("Вещь с Id = " + bookingDtoSimple.getItemId() +
                    " не доступна для аренды");
        }
        if (item.getOwner().getId() == userId) {
            throw new StorageException("Владелец вещи не может забронировать свою вещь");
        } else {
            booking.setItem(item);
            return mapper.toBookingDto(bookingRepository.save(booking));
        }
    }

    @Override
    public BookingDto update(long bookingId, BookingDto bookingDto) {
        BookingDto oldBookingDto = mapper.toBookingDto(bookingRepository.findById(bookingId).orElseThrow());
        if (bookingDto.getStart() != null) {
            oldBookingDto.setStart(bookingDto.getStart());
        }
        if (bookingDto.getEnd() != null) {
            oldBookingDto.setEnd(bookingDto.getEnd());
        }
        if (bookingDto.getItem() != null) {
            oldBookingDto.setItem(bookingDto.getItem());
        }
        if (bookingDto.getBooker() != null) {
            oldBookingDto.setBooker(bookingDto.getBooker());
        }
        if (bookingDto.getStatus() != null) {
            oldBookingDto.setStatus(bookingDto.getStatus());
        }
        return mapper.toBookingDto(bookingRepository.save(mapper.toBooking(oldBookingDto)));
    }

    @Override
    public void deleteById(long bookingId) {
        bookingRepository.deleteById(bookingId);
    }

    @Override
    public BookingDto approve(long userId, long bookingId, Boolean approved) {
        BookingDto bookingDto = mapper.toBookingDto(bookingRepository.findById(bookingId).orElseThrow());
        if (bookingDto.getItem().getOwner().getId() != userId) {
            throw new StorageException("Подтвердить бронирование может только владелец вещи");
        }
        if (bookingDto.getStatus().equals(Status.APPROVED)) {
            throw new BookingException("Бронирование уже подтверждено");
        }
        if (approved == null) {
            throw new BookingException("Необходимо указать approved");
        } else if (approved) {
            bookingDto.setStatus(Status.APPROVED);
            return mapper.toBookingDto(bookingRepository.save(mapper.toBooking(bookingDto)));
        } else {
            bookingDto.setStatus(Status.REJECTED);
            return mapper.toBookingDto(bookingRepository.save(mapper.toBooking(bookingDto)));
        }
    }

    @Override
    public List<BookingDto> findAllByItemOwnerId(long userId, String state) {
        userRepository.findById(userId).orElseThrow(() -> new StorageException("Incorrect userId"));
        List<BookingDto> result = bookingRepository.searchBookingByItem_Owner_Id(userId).stream()
                .map(mapper::toBookingDto).collect(Collectors.toList());
        if (result.isEmpty()) {
            throw new StorageException("У пользователя нет вещей");
        }
        try {
            switch (Status.valueOf(state)) {
                case ALL:
                    result.sort(Comparator.comparing(BookingDto::getStart).reversed());
                    return result;
                case CURRENT:
                    return bookingRepository.findCurrentBookingsByItem_Owner_IdOrderByStartDesc(userId,
                            LocalDateTime.now()).stream().map(mapper::toBookingDto).collect(Collectors.toList());
                case PAST:
                    return bookingRepository.findBookingsByItem_Owner_IdAndEndIsBeforeOrderByStartDesc(userId,
                            LocalDateTime.now()).stream().map(mapper::toBookingDto).collect(Collectors.toList());
                case FUTURE:
                    return bookingRepository.searchBookingByItem_Owner_IdAndStartIsAfterOrderByStartDesc(userId,
                            LocalDateTime.now()).stream().map(mapper::toBookingDto).collect(Collectors.toList());
                case WAITING:
                    return bookingRepository.findBookingsByItem_Owner_IdOrderByStartDesc(userId).stream()
                            .filter(booking -> booking.getStatus().equals(Status.WAITING))
                            .map(mapper::toBookingDto).collect(Collectors.toList());
                case REJECTED:
                    return bookingRepository.findBookingsByItem_Owner_IdOrderByStartDesc(userId).stream()
                            .filter(booking -> booking.getStatus().equals(Status.REJECTED))
                            .map(mapper::toBookingDto).collect(Collectors.toList());
                default:
                    throw new BookingException("Unknown state: UNSUPPORTED_STATUS");
            }
        } catch (IllegalArgumentException e) {
            throw new BookingException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

}

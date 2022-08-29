package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoSimple;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.exception.ItemException;
import ru.practicum.shareit.exception.StorageException;
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
    public BookingServiceImpl(BookingRepository bookingRepository, ItemRepository itemRepository, UserRepository userRepository, BookingMapper mapper) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public BookingDto findById(long bookingId, long userId) {
        if (bookingRepository.findById(bookingId).isPresent()) {
            if (bookingRepository.findById(bookingId).get().getBooker().getId() != userId
                    && bookingRepository.findById(bookingId).get().getItem().getOwner().getId() != userId) {
                throw new StorageException("Incorrect userId");
            }
            return mapper.toBookingDto(bookingRepository.findById(bookingId).get());
        }
        throw new StorageException("Бронирования с Id = " + bookingId + " нет в БД");
    }

    @Override
    public List<BookingDto> findAll(long userId, String state) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new StorageException("Incorrect userId");
        }
        List<BookingDto> result = bookingRepository.findAll().stream()
                .filter(booking -> booking.getBooker().getId() == (userId))
                .map(mapper::toBookingDto).collect(Collectors.toList());
        switch (state) {
            case ("ALL"):
                result.sort(Comparator.comparing(BookingDto::getStart).reversed());
                break;
            case ("CURRENT"):
                return bookingRepository.findCurrentBookingsByBooker_IdOrderByStartDesc
                                (userId, LocalDateTime.now())
                        .stream()
                        .map(mapper::toBookingDto).collect(Collectors.toList());
            case ("PAST"):
                return bookingRepository.findBookingsByBooker_IdAndEndIsBeforeOrderByStartDesc
                                (userId, LocalDateTime.now())
                        .stream()
                        .map(mapper::toBookingDto).collect(Collectors.toList());
            case ("FUTURE"):
                return bookingRepository.findAllByStartAfterOrderByStartDesc(LocalDateTime.now()).stream()
                        .filter(booking -> booking.getBooker().getId().equals(result.get(0).getBooker().getId()))
                        .map(mapper::toBookingDto).collect(Collectors.toList());
            case ("WAITING"):
                return bookingRepository.findBookingsByBooker_IdOrderByStartDesc(userId)
                        .stream()
                        .filter(booking -> booking.getStatus().equals(Status.WAITING))
                        .map(mapper::toBookingDto).collect(Collectors.toList());
            case ("REJECTED"):
                return bookingRepository.findBookingsByBooker_IdOrderByStartDesc(userId)
                        .stream()
                        .filter(booking -> booking.getStatus().equals(Status.REJECTED))
                        .map(mapper::toBookingDto).collect(Collectors.toList());
            default:
                throw new BookingException("Unknown state: UNSUPPORTED_STATUS");
        }
        return result;
    }

    @Override
    public BookingDto save(BookingDtoSimple bookingDtoSimple, long userId) {
        if (bookingDtoSimple.getEnd().isBefore(bookingDtoSimple.getStart())) {
            throw new BookingException("Incorrect end time");
        }
        Booking booking = mapper.fromSimpleToBooking(bookingDtoSimple);
        booking.setBooker(userRepository.findById(userId).orElseThrow());
        if (itemRepository.findById(bookingDtoSimple.getItemId()).isEmpty()) {
            throw new StorageException("Вещи с Id = " + bookingDtoSimple.getItemId() + " нет в базе данных");
        }
        if (!itemRepository.findById(bookingDtoSimple.getItemId()).get().getAvailable()) {
            throw new ItemException("Вещь с Id = " + bookingDtoSimple.getItemId() + " не доступна для аренды");
        }
        if (itemRepository.findById(bookingDtoSimple.getItemId()).get().getOwner().getId() == userId) {
            throw new StorageException("Владелец вещи не может забронировать свою вещь");
        } else {
            booking.setItem(itemRepository.findById(bookingDtoSimple.getItemId()).get());
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
    public List<BookingDto> findAllByOwnerId(long userId, String state) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new StorageException("Incorrect userId");
        }
        List<BookingDto> result = bookingRepository.searchBookingByItem_Owner_Id(userId).stream()
                .map(mapper::toBookingDto).collect(Collectors.toList());
        if (result.isEmpty()) {
            throw new StorageException("У пользователя нет вещей");
        }
        switch (state) {
            case ("ALL"):
                result.sort(Comparator.comparing(BookingDto::getStart).reversed());
                break;
            case ("CURRENT"):
                return bookingRepository.findCurrentBookingsByItem_Owner_IdOrderByStartDesc(userId, LocalDateTime.now())
                        .stream()
                        .map(mapper::toBookingDto).collect(Collectors.toList());
            case ("PAST"):
                return bookingRepository.findBookingsByItem_Owner_IdAndEndIsBeforeOrderByStartDesc
                                (userId, LocalDateTime.now())
                        .stream()
                        .map(mapper::toBookingDto).collect(Collectors.toList());
            case ("FUTURE"):
                return bookingRepository.searchBookingByItem_Owner_IdAndStartIsAfterOrderByStartDesc(userId, LocalDateTime.now())
                        .stream().map(mapper::toBookingDto).collect(Collectors.toList());
            case ("WAITING"):
                return bookingRepository.findBookingsByItem_Owner_IdOrderByStartDesc(userId)
                        .stream()
                        .filter(booking -> booking.getStatus().equals(Status.WAITING))
                        .map(mapper::toBookingDto).collect(Collectors.toList());
            case ("REJECTED"):
                return bookingRepository.findBookingsByItem_Owner_IdOrderByStartDesc(userId)
                        .stream()
                        .filter(booking -> booking.getStatus().equals(Status.REJECTED))
                        .map(mapper::toBookingDto).collect(Collectors.toList());
            default:
                throw new BookingException("Unknown state: UNSUPPORTED_STATUS");
        }
        return result;
    }

}

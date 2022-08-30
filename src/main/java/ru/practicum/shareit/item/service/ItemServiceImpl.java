package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.exception.StorageException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, ItemMapper itemMapper,
                           UserRepository userRepository, BookingRepository bookingRepository,
                           BookingMapper bookingMapper, CommentRepository commentRepository,
                           CommentMapper commentMapper) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
    }

    @Override
    public ItemDtoWithBooking findById(long itemId, long userId) {
        if (itemRepository.findById(itemId).isPresent()) {
            ItemDtoWithBooking itemDtoWithBooking = itemMapper
                    .toItemDtoWithBooking(itemRepository.findById(itemId).get());
            if (itemRepository.findById(itemId).get().getOwner().getId() == userId) {
                createItemDtoWithBooking(itemDtoWithBooking);
            }
            if (!commentRepository.findAllByItem_Id(itemId).isEmpty()) {
                itemDtoWithBooking.setComments(commentRepository.findAllByItem_Id(itemId)
                        .stream().map(commentMapper::toCommentDto)
                        .collect(Collectors.toList())
                );
            }
            return itemDtoWithBooking;
        }
        throw new StorageException("Вещи с Id = " + itemId + " нет в БД");
    }

    @Override
    public List<ItemDtoWithBooking> findAll(long userId) {
        List<ItemDtoWithBooking> result = itemRepository.findAll().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .map(itemMapper::toItemDtoWithBooking)
                .collect(Collectors.toList());
        for (ItemDtoWithBooking itemDtoWithBooking : result) {
            createItemDtoWithBooking(itemDtoWithBooking);
            if (!commentRepository.findAllByItem_Id(itemDtoWithBooking.getId()).isEmpty()) {
                itemDtoWithBooking.setComments(commentRepository.findAllByItem_Id(itemDtoWithBooking.getId())
                        .stream().map(commentMapper::toCommentDto)
                        .collect(Collectors.toList())
                );
            }
        }
        result.sort(Comparator.comparing(ItemDtoWithBooking::getId));
        return result;
    }

    private void createItemDtoWithBooking(ItemDtoWithBooking itemDtoWithBooking) {
        if (!bookingRepository
                .findBookingsByItem_IdAndEndIsBeforeOrderByEndDesc(itemDtoWithBooking.getId(),
                        LocalDateTime.now()).isEmpty()) {
            BookingDtoForItem lastBooking = bookingMapper.toBookingDtoForItem(bookingRepository
                    .findBookingsByItem_IdAndEndIsBeforeOrderByEndDesc(itemDtoWithBooking.getId(),
                            LocalDateTime.now()).get(0));
            itemDtoWithBooking.setLastBooking(lastBooking);
        }
        if (!bookingRepository
                .findBookingsByItem_IdAndStartIsAfterOrderByStartDesc(itemDtoWithBooking.getId(),
                        LocalDateTime.now()).isEmpty()) {
            BookingDtoForItem nextBooking = bookingMapper.toBookingDtoForItem(bookingRepository
                    .findBookingsByItem_IdAndStartIsAfterOrderByStartDesc(itemDtoWithBooking.getId(),
                            LocalDateTime.now()).get(0));
            itemDtoWithBooking.setNextBooking(nextBooking);
        }
    }

    @Override
    public ItemDto save(ItemDto itemDto, long userId) {
        Item item = itemMapper.toItem(itemDto);
        try {
            item.setOwner(userRepository.findById(userId).orElseThrow());
            return itemMapper.toItemDto(itemRepository.save(item));
        } catch (Exception exception) {
            throw new StorageException("Incorrect userId");
        }
    }

    @Override
    public CommentDto saveComment(long userId, long itemId, CommentDto commentDto) {
        itemRepository.findById(itemId).orElseThrow(() ->
                new StorageException("Вещи с Id = " + itemId + " нет в БД"));
        userRepository.findById(userId).orElseThrow(() ->
                new StorageException("Пользователя с Id = " + userId + " нет в БД"));
        if (bookingRepository.searchBookingByBooker_IdAndItem_IdAndEndIsBefore(userId, itemId, LocalDateTime.now())
                .stream().noneMatch(booking -> booking.getStatus().equals(Status.APPROVED))
        ) {
            throw new BookingException("Пользователь с Id = " + userId + " не брал в аренду вещь с Id = " + itemId);
        }
        Comment comment = commentMapper.toComment(commentDto);
        comment.setItem(itemRepository.findById(itemId).orElse(null));
        comment.setAuthor(userRepository.findById(userId).orElse(null));
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public ItemDto update(ItemDto itemDto, long userId, long id) {

        try {
            Item oldItem = itemRepository.findById(id).orElseThrow();

            if (oldItem.getOwner().getId() == userId) {

                if (itemDto.getName() != null) {
                    oldItem.setName(itemDto.getName());
                }
                if (itemDto.getDescription() != null) {
                    oldItem.setDescription(itemDto.getDescription());
                }
                if (itemDto.getAvailable() != null) {
                    oldItem.setAvailable(itemDto.getAvailable());
                }
                return itemMapper.toItemDto(itemRepository.save(oldItem));
            } else {
                throw new StorageException("Incorrect userId");
            }
        } catch (Exception e) {
            throw new StorageException("Incorrect ItemId");
        }
    }

    @Override
    public void deleteById(long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        if (!text.isBlank()) {
            return itemRepository.search(text)
                    .stream()
                    .filter(Item::getAvailable)
                    .map(itemMapper::toItemDto)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

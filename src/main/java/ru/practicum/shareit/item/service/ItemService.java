package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;

import java.util.List;

public interface ItemService {

    ItemDtoWithBooking findById(long itemId, long userId);

    List<ItemDtoWithBooking> findAll(long userId);

    ItemDto save(ItemDto itemDto, long userId);

    ItemDto update(ItemDto itemDto, long userId, long id);

    void deleteById(long itemId);

    List<ItemDto> searchItem(String text);

    CommentDto saveComment(long userId, long itemId, CommentDto commentDto);
}

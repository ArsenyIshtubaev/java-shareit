package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto findById(long itemId);

    List<ItemDto> findAll(long userId);

    ItemDto save(ItemDto itemDto, long userId);

    ItemDto update(ItemDto itemDto, long userId, long id);

    void deleteById(long itemId);

    List<ItemDto> searchItem(String text);
}

package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto findById(long itemId);
    List<ItemDto> findAll();
    ItemDto save(ItemDto itemDto);
    ItemDto update(ItemDto itemDto);
    void deleteById(long itemId);
}

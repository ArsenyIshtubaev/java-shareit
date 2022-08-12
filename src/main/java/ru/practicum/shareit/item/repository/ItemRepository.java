package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {

    Optional<Item> findById(long itemId);

    List<Item> findAll();

    Item save(Item item);

    Item update(Item item);

    void deleteById(long itemId);

    List<Item> searchItem(String text);
}

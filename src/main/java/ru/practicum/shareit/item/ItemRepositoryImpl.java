package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ItemRepositoryImpl implements ItemRepository {

    private final List<Item> items = new ArrayList<>();

    @Override
    public Optional<Item> findById(long itemId) {
        return items.stream()
                .filter(item -> item.getId() == itemId)
                .findFirst();
    }

    @Override
    public List<Item> findAll() {
        return items;
    }

    @Override
    public Item save(Item item) {
        item.setId(getId());
        items.add(item);
        return item;
    }

    @Override
    public Item update(Item item) {
        deleteById(item.getId());
        items.add(item);
        return item;
    }

    @Override
    public void deleteById(long itemId) {
        items.remove(findById(itemId).get());
    }

    private long getId() {
        long lastId = items.stream()
                .mapToLong(Item::getId)
                .max()
                .orElse(0);
        return lastId + 1;
    }
}

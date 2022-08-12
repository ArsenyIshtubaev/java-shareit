package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ItemRepositoryImpl implements ItemRepository {

    private final List<Item> items = new ArrayList<>();

    private static long id = 0;

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
        item.setId(generateId());
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

    @Override
    public List<Item> searchItem(String text) {
        List<Item> findItems = new ArrayList<>();
        for (Item item : items) {
            String search = (item.getName() + item.getDescription()).toLowerCase();
            if (!text.isBlank() && search.contains(text.toLowerCase()) && item.getAvailable()) {
                findItems.add(item);
            }
        }
        return findItems;
    }

    private long generateId() {
        return ++id;
    }
}

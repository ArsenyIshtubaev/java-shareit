package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.requests.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRequestRepository itemRequestRepository;

    private Item item;
    private User user1;
    private User user2;
    private ItemRequest itemRequest;

    @BeforeEach
    void beforeEach() {
        user1 = userRepository.save(new User(1L, "user1", "user1@mail.ru"));
        user2 = userRepository.save(new User(2L, "user2", "user2@mail.ru"));
        itemRequest = itemRequestRepository.save(new ItemRequest(1L, "itemRequest1",
                user2, LocalDateTime.now()));
        item = itemRepository.save(new Item(1L, "item1", "description1",
                true, user1, itemRequest));
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
    }

    @Test
    void search() {
        final String text = item.getDescription().substring(0, 3);
        final List<Item> items = itemRepository.search(text, Pageable.unpaged());
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item, items.get(0));

        final String incorrectText = "incorrect";
        final List<Item> emptyItems = itemRepository.search(incorrectText, Pageable.unpaged());
        assertTrue(emptyItems.isEmpty());
    }

    @Test
    void findAllByItemRequest_Id() {
        final List<Item> items = itemRepository.findAllByItemRequest_Id(itemRequest.getId());
        assertSame(user2, itemRequest.getRequestor());
        assertNotNull(items);
        assertEquals(1, items.size());
        assertSame(item, items.get(0));
    }

    @Test
    void findByOwnerId() {
        final List<Item> byOwner = itemRepository.findByOwnerId(user1.getId(), Pageable.unpaged());
        assertNotNull(byOwner);
        assertEquals(1, byOwner.size());
        assertSame(item, byOwner.get(0));
    }
}
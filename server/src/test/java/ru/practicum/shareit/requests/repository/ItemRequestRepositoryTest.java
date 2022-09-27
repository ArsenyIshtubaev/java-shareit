package ru.practicum.shareit.requests.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    ItemRepository itemRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRequestRepository itemRequestRepository;

    private User user2;
    private ItemRequest itemRequest;

    @BeforeEach
    void beforeEach() {
        user2 = userRepository.save(new User(2L, "user2", "user2@mail.ru"));
        itemRequest = itemRequestRepository.save(new ItemRequest(1L, "itemRequest1",
                user2, LocalDateTime.now()));
    }

    @AfterEach
    void afterEach() {
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void findAllByRequestor_IdOrderByCreatedDesc() {
        final List<ItemRequest> requests = itemRequestRepository
                .findAllByRequestor_IdOrderByCreatedDesc(itemRequest.getRequestor().getId());
        assertSame(user2, itemRequest.getRequestor());
        assertNotNull(requests);
        assertEquals(1, requests.size());
        assertSame(itemRequest, requests.get(0));
    }

    @Test
    void findAll() {
        final Page<ItemRequest> pageRequests = itemRequestRepository
                .findAll(Pageable.unpaged());
        List<ItemRequest> requests = pageRequests.getContent();
        assertNotNull(requests);
        assertEquals(1, requests.size());
        assertSame(itemRequest, requests.get(0));
    }
}
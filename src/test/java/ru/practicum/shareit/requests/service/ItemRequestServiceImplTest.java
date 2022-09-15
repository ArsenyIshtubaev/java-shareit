package ru.practicum.shareit.requests.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.StorageException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.requests.dto.ItemRequestMapper;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.requests.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemRequestServiceImplTest {

    private ItemRequestRepository itemRequestRepository;
    private ItemRequestMapper itemRequestMapper;
    private UserRepository userRepository;
    private ItemRequestService itemRequestService;
    private ItemRequest itemRequest;

    @BeforeEach
    void beforeEach() {
        userRepository = mock(UserRepository.class);
        itemRequestRepository = mock(ItemRequestRepository.class);
        ItemRepository itemRepository = mock(ItemRepository.class);
        ItemMapper itemMapper = mock(ItemMapper.class);
        itemRequestMapper = new ItemRequestMapper(itemRepository, itemMapper);
        itemRequestService = new ItemRequestServiceImpl(itemRequestRepository,
                itemRequestMapper, userRepository);
        itemRequest = createItemRequest();
    }

    private ItemRequest createItemRequest() {
        User user2 = new User(2L, "user2", "user2@mail.ru");
        return new ItemRequest(1L, "itemRequest1", user2, LocalDateTime.now());
    }

    @Test
    void save() {
        when(itemRequestRepository.save(any(ItemRequest.class)))
                .thenReturn(itemRequest);
        when(userRepository.findById(itemRequest.getRequestor().getId()))
                .thenReturn(Optional.of(itemRequest.getRequestor()));
        ItemRequestDto itemRequestDto = itemRequestService
                .save(itemRequestMapper.toItemRequestDto(itemRequest),
                        itemRequest.getRequestor().getId());
        assertNotNull(itemRequestDto);
        assertEquals("itemRequest1", itemRequestDto.getDescription());
        assertEquals("user2", itemRequest.getRequestor().getName());
        assertEquals(itemRequest.getId(), itemRequestDto.getId());
        verify(itemRequestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void findAll() {
        when(userRepository.findById(itemRequest.getRequestor().getId()))
                .thenReturn(Optional.of(itemRequest.getRequestor()));
        when(itemRequestRepository
                .findAllByRequestor_IdOrderByCreatedDesc(itemRequest.getRequestor().getId()))
                .thenReturn(Collections.singletonList(itemRequest));
        final List<ItemRequestDtoWithItems> itemRequestDtoWithItems = itemRequestService
                .findAll(itemRequest.getRequestor().getId());
        assertNotNull(itemRequestDtoWithItems);
        assertEquals(1, itemRequestDtoWithItems.size());
        assertEquals(itemRequest.getDescription(), itemRequestDtoWithItems.get(0).getDescription());
        verify(itemRequestRepository, times(1))
                .findAllByRequestor_IdOrderByCreatedDesc(itemRequest.getRequestor().getId());
    }

    @Test
    void findById() {
        Long itemRequestId = itemRequest.getId();
        when(userRepository.findById(itemRequest.getRequestor().getId()))
                .thenReturn(Optional.of(itemRequest.getRequestor()));
        long incorrectId = (long) (Math.random() * 100) + itemRequestId + 3;
        when(itemRequestRepository.findById(itemRequestId))
                .thenReturn(Optional.of(itemRequest));
        when(itemRequestRepository.findById(incorrectId))
                .thenThrow(new StorageException("Запроса с Id = " + incorrectId + " нет в БД"));
        ItemRequestDtoWithItems itemRequestDtoWithItems = itemRequestService
                .findById(itemRequest.getRequestor().getId(), itemRequestId);
        assertNotNull(itemRequestDtoWithItems);
        assertEquals("itemRequest1", itemRequestDtoWithItems.getDescription());
        Throwable thrown = assertThrows(StorageException.class,
                () -> itemRequestService.findById(itemRequest.getRequestor().getId(), incorrectId));
        assertNotNull(thrown.getMessage());
        verify(itemRequestRepository, times(1)).findById(itemRequestId);
    }

    @Test
    void findAllWithPageable() {
        when(userRepository.findById(itemRequest.getRequestor().getId()))
                .thenReturn(Optional.of(itemRequest.getRequestor()));
        when(itemRequestRepository
                .findAll(PageRequest.of(0, 20, Sort.by("created"))))
                .thenReturn(Page.empty());
        final List<ItemRequestDtoWithItems> itemRequestDtoWithItems = itemRequestService
                .findAllWithPageable(itemRequest.getRequestor().getId(), 0, 20);
        assertNotNull(itemRequestDtoWithItems);
        assertTrue(itemRequestDtoWithItems.isEmpty());
        verify(itemRequestRepository, times(1))
                .findAll(PageRequest.of(0, 20, Sort.by("created")));
    }
}
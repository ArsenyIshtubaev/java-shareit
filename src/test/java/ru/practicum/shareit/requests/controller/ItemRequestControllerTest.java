package ru.practicum.shareit.requests.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.requests.dto.ItemRequestMapper;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.requests.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {

    @Mock
    private ItemRequestService itemRequestService;
    @InjectMocks
    private ItemRequestController itemRequestController;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private ItemRequestMapper itemRequestMapper;

    private ItemRequest itemRequest;

    @BeforeEach
    void beforeEach() {
        ItemRepository itemRepository = mock(ItemRepository.class);
        ItemMapper itemMapper = mock(ItemMapper.class);
        itemRequestMapper = new ItemRequestMapper(itemRepository, itemMapper);
        mockMvc = MockMvcBuilders
                .standaloneSetup(itemRequestController)
                .build();
        mapper.registerModule(new JavaTimeModule());
        itemRequest = createItemRequest();
    }

    private ItemRequest createItemRequest() {
        User user2 = new User(2L, "user2", "user2@mail.ru");
        return new ItemRequest(1L, "itemRequest1", user2, LocalDateTime.now());
    }

    @Test
    void add() throws Exception {
        ItemRequestDto itemRequestDto = itemRequestMapper.toItemRequestDto(itemRequest);
        when(itemRequestService.save(itemRequestDto, 2)).thenReturn(itemRequestDto);
        mockMvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1," +
                        " \"description\": \"itemRequest1\"}"));
        verify(itemRequestService, times(1))
                .save(itemRequestDto, itemRequest.getRequestor().getId());
    }

    @Test
    void findAll() throws Exception {
        List<ItemRequestDtoWithItems> result = new ArrayList<>();
        ItemRequestDtoWithItems itemRequestDtoWithItems = itemRequestMapper
                .toItemRequestDtoWithItems(itemRequest);
        result.add(itemRequestDtoWithItems);
        when(itemRequestService.findAll(itemRequest.getRequestor().getId()))
                .thenReturn(result);
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", itemRequest.getRequestor().getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\": 1," +
                        " \"description\": \"itemRequest1\"}]"));
        verify(itemRequestService, times(1))
                .findAll(itemRequest.getRequestor().getId());
    }

    @Test
    void findById() throws Exception {
        List<ItemRequestDtoWithItems> result = new ArrayList<>();
        ItemRequestDtoWithItems itemRequestDtoWithItems = itemRequestMapper
                .toItemRequestDtoWithItems(itemRequest);
        result.add(itemRequestDtoWithItems);
        when(itemRequestService.findAllWithPageable(itemRequest.getRequestor().getId(),
                0, 20))
                .thenReturn(result);
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", itemRequest.getRequestor().getId())
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\": 1," +
                        " \"description\": \"itemRequest1\"}]"));
        verify(itemRequestService, times(1))
                .findAllWithPageable(itemRequest.getRequestor().getId(), 0, 20);
    }

    @Test
    void findByRequestId() throws Exception {
        ItemRequestDtoWithItems itemRequestDtoWithItems = itemRequestMapper
                .toItemRequestDtoWithItems(itemRequest);
        when(itemRequestService.findById(itemRequest.getRequestor().getId(), itemRequest.getId()))
                .thenReturn(itemRequestDtoWithItems);
        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", itemRequest.getRequestor().getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1," +
                        " \"description\": \"itemRequest1\"}"));
        verify(itemRequestService, times(1))
                .findById(itemRequest.getRequestor().getId(), itemRequest.getId());
    }
}
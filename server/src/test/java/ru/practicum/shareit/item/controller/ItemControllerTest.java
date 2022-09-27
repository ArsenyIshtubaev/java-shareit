package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
class ItemControllerTest {
    @MockBean
    private ItemService itemService;
    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ItemMapper itemMapper = new ItemMapper();

    private Item createItem() {
        User user1 = new User(1L, "user1", "user1@mail.ru");
        User user2 = new User(2L, "user2", "user2@mail.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "itemRequest1",
                user2, LocalDateTime.now());
        return new Item(1L, "item1", "description1",
                true, user1, itemRequest);
    }

    private CommentDto createCommentDto() {
        return new CommentDto(1L, "Great", "user1", LocalDateTime.now());
    }

    @Test
    void findAll() throws Exception {
        List<ItemDtoWithBooking> items = new ArrayList<>();
        Item item = createItem();
        ItemDtoWithBooking itemDtoWithBooking = itemMapper.toItemDtoWithBooking(item);
        items.add(itemDtoWithBooking);
        when(itemService.findAll(item.getOwner().getId(), 0, 20)).thenReturn(items);
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", item.getOwner().getId())
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\": 1,\"name\": \"item1\"," +
                        " \"description\": \"description1\", \"available\": true," +
                        " \"lastBooking\": null, \"nextBooking\": null, \"comments\": []}]"));
        verify(itemService, times(1))
                .findAll(item.getOwner().getId(), 0, 20);
    }

    @Test
    void create() throws Exception {
        Item item = createItem();
        ItemDto itemDto = itemMapper.toItemDto(item);
        when(itemService.save(item.getOwner().getId(), itemDto)).thenReturn(itemDto);
        mockMvc.perform(post("/items").content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", item.getOwner().getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1,\"name\": \"item1\"," +
                        " \"description\": \"description1\", \"available\": true, \"requestId\": 1}"));
        verify(itemService, times(1)).save(item.getOwner().getId(), itemDto);
    }

    @Test
    void update() throws Exception {
        Item item = createItem();
        ItemDto itemDto = itemMapper.toItemDto(item);
        Item item2 = createItem();
        ItemDto itemDto2 = itemMapper.toItemDto(item2);
        itemDto2.setName("item2");
        itemService.save(item.getOwner().getId(), itemDto);
        when(itemService.update(item.getOwner().getId(), itemDto.getId(), itemDto2)).thenReturn(itemDto2);
        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", item.getOwner().getId())
                        .content(mapper.writeValueAsString(itemDto2))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1,\"name\": \"item2\"," +
                        " \"description\": \"description1\", \"available\": true, \"requestId\": 1}"));
        verify(itemService, times(1))
                .update(item.getOwner().getId(), itemDto.getId(), itemDto2);
    }

    @Test
    void createComment() throws Exception {
        mapper.registerModule(new JavaTimeModule());
        CommentDto commentDto = createCommentDto();
        Item item = createItem();
        when(itemService.saveComment(1, 1, commentDto))
                .thenReturn(commentDto);
        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1,\"text\": \"Great\"," +
                        " \"authorName\": \"user1\"}"));
        verify(itemService, times(1))
                .saveComment(item.getOwner().getId(), item.getId(), commentDto);
    }

    @Test
    void findItemById() throws Exception {
        Item item = createItem();
        ItemDtoWithBooking itemDtoWithBooking = itemMapper.toItemDtoWithBooking(item);
        when(itemService.findById(1, item.getOwner().getId())).thenReturn(itemDtoWithBooking);
        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", item.getOwner().getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1,\"name\": \"item1\"," +
                        " \"description\": \"description1\", \"available\": true," +
                        " \"lastBooking\": null, \"nextBooking\": null, \"comments\": []}"));
        verify(itemService, times(1)).findById(1, 1);
    }

    @Test
    void deleteItemById() throws Exception {
        mockMvc.perform(delete("/items/1"))
                .andExpect(status().isOk());
        verify(itemService, times(1)).deleteById(1);
    }

    @Test
    void findItemByText() throws Exception {
        List<ItemDto> items = new ArrayList<>();
        Item item = createItem();
        ItemDto itemDto = itemMapper.toItemDto(item);
        items.add(itemDto);
        String text = item.getDescription().substring(0, 3);
        when(itemService.searchItem(text, 0, 20)).thenReturn(items);
        mockMvc.perform(get("/items/search")
                        .param("text", text)
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\": 1,\"name\": \"item1\"," +
                        " \"description\": \"description1\", \"available\": true, \"requestId\": 1}]"));
        verify(itemService, times(1))
                .searchItem(text, 0, 20);
    }
}
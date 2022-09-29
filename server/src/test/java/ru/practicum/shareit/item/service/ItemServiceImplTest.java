package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.StorageException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.requests.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceImplTest {

    private ItemService itemService;
    private ItemRepository itemRepository;
    private ItemMapper itemMapper;
    private UserRepository userRepository;
    private ItemRequestRepository itemRequestRepository;
    private BookingRepository bookingRepository;
    private CommentRepository commentRepository;
    private CommentMapper commentMapper;

    @BeforeEach
    void beforeEach() {
        itemRepository = mock(ItemRepository.class);
        userRepository = mock(UserRepository.class);
        bookingRepository = mock(BookingRepository.class);
        commentRepository = mock(CommentRepository.class);
        itemRequestRepository = mock(ItemRequestRepository.class);
        itemMapper = new ItemMapper();
        BookingMapper bookingMapper = new BookingMapper();
        commentMapper = new CommentMapper();
        itemService = new ItemServiceImpl(itemRepository, itemMapper,
                userRepository, bookingRepository, bookingMapper,
                commentRepository, commentMapper, itemRequestRepository);
    }

    private Item createItem() {
        User user1 = new User(1L, "user1", "user1@mail.ru");
        User user2 = new User(2L, "user2", "user2@mail.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "itemRequest1",
                user2, LocalDateTime.now());
        return new Item(1L, "item1", "description1",
                true, user1, itemRequest);
    }

    @Test
    void findById() {
        Item item = createItem();
        Long itemId = item.getId();
        long incorrectId = (long) (Math.random() * 100) + itemId + 3;
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));
        when(itemRepository.findById(incorrectId))
                .thenThrow(new StorageException("Вещи с Id = " + incorrectId + " нет в БД"));
        ItemDtoWithBooking itemDtoWithBooking = itemService.findById(itemId, item.getOwner().getId());
        assertNotNull(itemDtoWithBooking);
        assertEquals("item1", itemDtoWithBooking.getName());
        Throwable thrown = assertThrows(StorageException.class,
                () -> itemService.findById(incorrectId, item.getOwner().getId()));
        assertNotNull(thrown.getMessage());
        verify(itemRepository, times(1)).findById(itemId);
    }

    @Test
    void findAll() {
        Item item = createItem();
        User userWriteComment = item.getItemRequest().getRequestor();
        Comment comment = createComment(item, userWriteComment);
        when(commentRepository.findAllByItem_Id(item.getId()))
                .thenReturn(Collections.singletonList(comment));
        when(itemRepository.findByOwnerId(item.getOwner().getId(), PageRequest.of(0, 20)))
                .thenReturn(Collections.singletonList(item));
        final List<ItemDtoWithBooking> items = itemService
                .findAll(item.getOwner().getId(), 0, 20);
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item.getName(), items.get(0).getName());
        verify(itemRepository, times(1))
                .findByOwnerId(item.getOwner().getId(), PageRequest.of(0, 20));
    }

    @Test
    void save() {
        Item item = createItem();
        when(itemRepository.save(item))
                .thenReturn(item);
        when(userRepository.findById(item.getOwner().getId()))
                .thenReturn(Optional.of(item.getOwner()));
        when(itemRequestRepository.findById(item.getItemRequest().getId()))
                .thenReturn(Optional.of(item.getItemRequest()));
        ItemDto itemDto = itemService.save(item.getOwner().getId(), itemMapper.toItemDto(item));
        assertNotNull(itemDto);
        assertEquals("item1", itemDto.getName());
        assertEquals("description1", itemDto.getDescription());
        assertEquals(item.getId(), itemDto.getId());
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void saveComment() {
        Item item = createItem();
        User userWriteComment = item.getItemRequest().getRequestor();
        Comment comment = createComment(item, userWriteComment);
        Booking booking = createBooking(item, userWriteComment);
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(userRepository.findById(userWriteComment.getId()))
                .thenReturn(Optional.of(userWriteComment));
        List<Booking> bookingsList = new ArrayList<>();
        bookingsList.add(booking);
        when(bookingRepository
                .searchBookingByBooker_IdAndItem_IdAndEndIsBeforeAndStatus(anyLong(), anyLong(), any(), any()))
                .thenReturn(bookingsList);
        when(commentRepository.save(comment))
                .thenReturn(comment);
        CommentDto commentDto1 = commentMapper.toCommentDto(comment);
        CommentDto commentDto = itemService.saveComment(userWriteComment.getId(), item.getId(),
                commentDto1);
        assertNotNull(commentDto);
        assertEquals("Great", commentDto.getText());
        assertEquals(userWriteComment.getName(), commentDto.getAuthorName());
        assertEquals(comment.getId(), commentDto.getId());
        verify(commentRepository, times(1)).save(any());
        long incorrectUserId = 10L;
        Throwable thrown = assertThrows(StorageException.class,
                () -> itemService.saveComment(incorrectUserId,
                        item.getOwner().getId(), commentDto1));
        assertNotNull(thrown.getMessage());
        long incorrectItemId = 10L;
        Throwable thrown2 = assertThrows(StorageException.class,
                () -> itemService.saveComment(userWriteComment.getId(),
                        incorrectItemId, commentDto1));
        assertNotNull(thrown2.getMessage());
    }

    private Comment createComment(Item item, User user) {
        return new Comment(1L, "Great", item, user, LocalDateTime.now());
    }

    private Booking createBooking(Item item, User userWriteComment) {
        return new Booking(1L, LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(2),
                item, userWriteComment, Status.APPROVED);
    }

    @Test
    void update() {
        Item item = createItem();
        Item item2 = createItem();
        long itemId = item.getId();
        item2.setName("item2");
        when(itemRepository.save(any(Item.class))).thenReturn(item2);
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));
        ItemDto itemDto = itemService.update(item.getOwner().getId(), itemId, itemMapper.toItemDto(item2));
        assertNotNull(itemDto);
        assertEquals("item2", itemDto.getName());
        assertEquals("description1", itemDto.getDescription());
        assertEquals(item.getId(), itemDto.getId());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void deleteById() {
        Item item = createItem();
        itemService.deleteById(item.getId());
        verify(itemRepository, times(1)).deleteById(item.getId());
    }

    @Test
    void searchItem() {
        List<Item> items = new ArrayList<>();
        Item item = createItem();
        items.add(item);
        String text = item.getDescription().substring(0, 3);
        when(itemRepository.search(text, PageRequest.of(0, 20))).thenReturn(items);
        List<ItemDto> itemDtos = itemService.searchItem(text, 0, 20);
        assertNotNull(itemDtos);
        assertEquals(1, itemDtos.size());
        assertEquals(item.getName(), itemDtos.get(0).getName());
        verify(itemRepository, times(1))
                .search(text, PageRequest.of(0, 20));
    }
}
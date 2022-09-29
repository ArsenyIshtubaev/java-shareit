package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    ItemRepository itemRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CommentRepository commentRepository;

    private Item item;
    private User user;
    private Comment comment;

    @BeforeEach
    void beforeEach() {
        user = userRepository.save(new User(1L, "user", "user@mail.ru"));
        item = itemRepository.save(new Item(1L, "item", "description",
                true, user, null));
        comment = commentRepository.save(new Comment(1L, "Great", item, user, LocalDateTime.now()));
    }

    @AfterEach
    void afterEach() {
        commentRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findAllByItem_Id() {
        final List<Comment> comments = commentRepository.findAllByItem_Id(item.getId());
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertSame(comment, comments.get(0));
    }
}
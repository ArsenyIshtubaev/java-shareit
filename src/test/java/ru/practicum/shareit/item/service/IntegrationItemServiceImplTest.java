package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class IntegrationItemServiceImplTest {

    private final EntityManager em;
    private final ItemService service;
    private final UserService userService;
    private final ItemRequestService itemRequestService;

    private UserDto user1Dto;
    private UserDto user2Dto;
    private ItemRequestDto itemRequestDto;
    private ItemDto itemDto;

    @BeforeEach
    void beforeEach() {
        user1Dto = makeUserDto("some@email.com", "Пётр");
        user2Dto = makeUserDto("user2@email.com", "user2");
        itemRequestDto = makeItemRequestDto();
        itemDto = createItemDto();
        user1Dto = userService.save(user1Dto);
        user2Dto = userService.save(user2Dto);
        itemRequestDto = itemRequestService.save(itemRequestDto, user2Dto.getId());
        itemDto = service.save(itemDto, user1Dto.getId());
    }

    @AfterEach
    void afterEach() {
        userService.deleteById(user1Dto.getId());
        userService.deleteById(user2Dto.getId());
        itemRequestService.deleteById(itemRequestDto.getId());
        service.deleteById(itemDto.getId());
    }

    private ItemDto createItemDto() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("item1");
        itemDto.setDescription("description1");
        itemDto.setAvailable(true);
        itemDto.setRequestId(itemRequestDto.getId());
        return itemDto;
    }

    private UserDto makeUserDto(String email, String name) {
        UserDto dto = new UserDto();
        dto.setEmail(email);
        dto.setName(name);
        return dto;
    }

    private ItemRequestDto makeItemRequestDto() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("description1");
        dto.setCreated(LocalDateTime.now());
        return dto;
    }

    @Test
    void findById() {
        ItemDtoWithBooking itemDto1 = service.findById(itemDto.getId(), user1Dto.getId());
        assertThat(itemDto1.getId(), notNullValue());
        assertThat(itemDto1.getName(), equalTo(itemDto.getName()));
        assertThat(itemDto1.getDescription(), equalTo(itemDto.getDescription()));
    }

    @Test
    void save() {
        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.name = :name", Item.class);
        Item item = query.setParameter("name", itemDto.getName()).getSingleResult();
        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
    }
}
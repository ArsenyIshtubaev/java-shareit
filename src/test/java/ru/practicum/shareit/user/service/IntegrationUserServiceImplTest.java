package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class IntegrationUserServiceImplTest {

    private final EntityManager em;
    private final UserService service;

    @Test
    void save() {
        UserDto userDto = makeUserDto();
        service.save(userDto);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", userDto.getEmail()).getSingleResult();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
        service.deleteById(user.getId());
    }

    @Test
    void findById() {
        service.save(makeUserDto());
        UserDto userDto = service.findById(1);
        assertThat(userDto.getId(), notNullValue());
        assertThat(userDto.getName(), equalTo("Пётр"));
        assertThat(userDto.getEmail(), equalTo("some@email.com"));
        service.deleteById(userDto.getId());
    }

    private UserDto makeUserDto() {
        UserDto dto = new UserDto();
        dto.setEmail("some@email.com");
        dto.setName("Пётр");
        return dto;
    }
}
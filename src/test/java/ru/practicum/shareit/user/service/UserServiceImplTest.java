package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.StorageException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    UserService userService;
    UserRepository userRepository;
    UserMapper userMapper;

    @BeforeEach
    void beforeEach() {
        userRepository = mock(UserRepository.class);
        userMapper = new UserMapper();
        userService = new UserServiceImpl(userRepository, userMapper);
    }

    private User createUser() {
        return new User(1L, "user", "user@mail.ru");
    }

    @Test
    void findById() {
        User user = createUser();
        Long userId = user.getId();
        long incorrectId = (long) (Math.random() * 100) + userId + 3;
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(userRepository.findById(incorrectId))
                .thenThrow(new StorageException("Пользователя с Id = " + incorrectId + " нет в БД"));
        UserDto userDto = userService.findById(userId);
        assertNotNull(userDto);
        assertEquals("user", userDto.getName());
        Throwable thrown = assertThrows(StorageException.class, () -> {
            userService.findById(incorrectId);
        });
        assertNotNull(thrown.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findAll() {
        User user = createUser();
        when(userRepository.findAll())
                .thenReturn(Collections.singletonList(user));
        final List<UserDto> userDtos = userService.findAll();
        assertNotNull(userDtos);
        assertEquals(1, userDtos.size());
        assertEquals(user.getEmail(), userDtos.get(0).getEmail());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void save() {
        User user = createUser();
        when(userRepository.save(user))
                .thenReturn(user);
        UserDto userDto = userService.save(userMapper.toUserDto(user));
        assertNotNull(userDto);
        assertEquals("user", userDto.getName());
        assertEquals("user@mail.ru", userDto.getEmail());
        assertEquals(user.getId(), userDto.getId());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void update() {
        User user = createUser();
        User user2 = createUser();
        long userId = user.getId();
        user2.setName("user2");
        when(userRepository.save(any(User.class))).thenReturn(user2);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        UserDto userDto = userService.update(userId, userMapper.toUserDto(user2));
        assertNotNull(userDto);
        assertEquals("user2", userDto.getName());
        assertEquals("user@mail.ru", userDto.getEmail());
        assertEquals(user.getId(), userDto.getId());
        verify(userRepository, times(1)).save(user2);
    }

    @Test
    void deleteById() {
        User user = createUser();
        userService.deleteById(user.getId());
        verify(userRepository, times(1)).deleteById(user.getId());
    }
}
package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
class UserControllerTest {

    @MockBean
    private UserService userService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    private UserDto createUserDto() {
        return new UserDto(1L, "user", "user@mail.ru");
    }

    @Test
    void findAll() throws Exception {
        List<UserDto> users = new ArrayList<>();
        users.add(createUserDto());
        when(userService.findAll()).thenReturn(users);
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\": 1,\"name\": \"user\",\"email\": \"user@mail.ru\"}]"));

        verify(userService, times(1)).findAll();
    }

    @Test
    void create() throws Exception {
        UserDto userDto = createUserDto();
        when(userService.save(any(UserDto.class))).thenReturn(userDto);
        mockMvc.perform(post("/users").content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1,\"name\": \"user\",\"email\": \"user@mail.ru\"}"));
        verify(userService, times(1)).save(userDto);
    }

    @Test
    void findUserById() throws Exception {
        UserDto userDto = createUserDto();
        when(userService.findById(1)).thenReturn(userDto);
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1,\"name\": \"user\",\"email\": \"user@mail.ru\"}"));
        verify(userService, times(1)).findById(1);
    }

    @Test
    void deleteUserById() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
        verify(userService, times(1)).deleteById(1);
    }

    @Test
    void update() throws Exception {
        UserDto userDto = createUserDto();
        UserDto userDto2 = createUserDto();
        userDto2.setName("user2");
        userService.save(userDto);
        when(userService.update(1, userDto2)).thenReturn(userDto2);
        mockMvc.perform(patch("/users/1").content(mapper.writeValueAsString(userDto2))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1,\"name\": \"user2\",\"email\": \"user@mail.ru\"}"));
        verify(userService, times(1)).update(1, userDto2);
    }
}
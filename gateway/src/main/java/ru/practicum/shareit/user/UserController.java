package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * // TODO .
 */
@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> findAll() {
        return userClient.getUsers();
    }

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserDto userDto) {
        log.info("Получен запрос к эндпоинту: '{} {}', Пользователь: Имя: {} и Email: {}", "POST", "/users",
                userDto.getName(), userDto.getEmail());
        return userClient.save(userDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findUserById(@Min(1) @NotNull @PathVariable long id) {
        log.info("GET user id={}", id);
        return userClient.getUser(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUserById(@Min(1) @NotNull @PathVariable long id) {
        return userClient.deleteUser(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@Min(1) @NotNull @PathVariable long id, @RequestBody UserDto userDto) {
        log.info("PATCH user id={}", id);
        return userClient.updateUser(id, userDto);
    }
}

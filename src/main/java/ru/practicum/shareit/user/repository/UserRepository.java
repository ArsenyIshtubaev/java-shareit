package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(long userId);

    List<User> findAll();

    User save(User user);

    User update(User user);

    void deleteById(long userId);
}

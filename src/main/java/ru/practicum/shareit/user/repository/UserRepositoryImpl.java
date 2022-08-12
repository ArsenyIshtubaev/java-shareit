package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final List<User> users = new ArrayList<>();
    private static long id = 0;

    @Override
    public Optional<User> findById(long userId) {
        return users.stream()
                .filter(user -> user.getId() == userId)
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return users;
    }

    @Override
    public User save(User user) {
        user.setId(generateId());
        users.add(user);
        return user;
    }

    @Override
    public User update(User user) {
        deleteById(user.getId());
        users.add(user);
        return user;
    }

    @Override
    public void deleteById(long userId) {
        users.remove(findById(userId).get());
    }

    private long generateId() {
        return ++id;
    }
}

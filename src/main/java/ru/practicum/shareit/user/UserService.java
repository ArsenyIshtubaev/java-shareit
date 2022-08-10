package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto findById(long userId);
    List<UserDto> findAll();
    UserDto save(UserDto userDto);
    UserDto update(long userId, UserDto userDto);
    void deleteById(long userId);

}

package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.StorageException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto findById(long userId) {
        if (userRepository.findById(userId).isPresent()) {
            return userMapper.toUserDto(userRepository.findById(userId).get());
        }
        throw new StorageException("Пользователя с Id = " + userId + " нет в БД");
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto save(UserDto userDto) {
        return userMapper.toUserDto(userRepository.save(userMapper.toUser(userDto)));
    }

    @Override
    public UserDto update(long userId, UserDto userDto) {
        UserDto oldUserDto = findById(userId);
        if (userDto.getName() != null){
            oldUserDto.setName(userDto.getName());
        }
        if (userDto.getEmail() != null){
           // emailValidator.updateValidationList(oldUserDto, userDto);
            oldUserDto.setEmail(userDto.getEmail());
        }
        return userMapper.toUserDto(userRepository.update(userMapper.toUser(oldUserDto)));
    }

    @Override
    public void deleteById(long userId) {
        userRepository.deleteById(userId);
    }


}

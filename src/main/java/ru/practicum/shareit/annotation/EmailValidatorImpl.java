package ru.practicum.shareit.annotation;

import ru.practicum.shareit.exception.StorageException;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class EmailValidatorImpl implements ConstraintValidator<EmailValidation, UserDto> {

    List<String> mailList = new ArrayList<>();

    @Override
    public void initialize(EmailValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(UserDto userDto, ConstraintValidatorContext constraintValidatorContext) {
            if (mailList.contains(userDto.getEmail())) {
                throw new StorageException("Duplicate email");
            }
            mailList.add(userDto.getEmail());
        return true;
    }

    public void updateValidationList(UserDto oldUserDto, UserDto userDto){
       mailList.remove(oldUserDto.getEmail());
       mailList.add(userDto.getEmail());
    }
}

package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.annotation.EmailValidation;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EmailValidation(message = "email already exists in database")
public class UserDto {
    private Long id;
    @NotBlank(message = "name should not be blank")
    private String name;
    @Email(message = "incorrect email")
    @NotBlank(message = "email should not be blank")
    private String email;
}

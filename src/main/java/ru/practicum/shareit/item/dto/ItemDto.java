package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * // TODO .
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {

    private Long id;
    @NotBlank(message = "name should not be blank")
    @NotNull
    private String name;
    @NotNull
    @NotBlank(message = "name should not be blank")
    private String description;
    @NotNull
    private Boolean available;
}

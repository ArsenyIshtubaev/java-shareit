package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * // TODO .
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {

    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
}

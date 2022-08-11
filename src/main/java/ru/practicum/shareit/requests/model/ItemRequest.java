package ru.practicum.shareit.requests.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * // TODO .
 */
@Data
@NoArgsConstructor
public class ItemRequest {

    private Long id;
    private String description;
    private User requestor;
    private LocalDateTime created;
}

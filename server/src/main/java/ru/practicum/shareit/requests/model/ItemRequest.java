package ru.practicum.shareit.requests.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * // TODO .
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "requests", schema = "public")
public class ItemRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;
    @Column(name = "description", nullable = false)
    private String description;
    @ManyToOne(optional = false)
    @JoinColumn(name = "requestor_id")
    private User requestor;
    @Column(name = "created")
    private LocalDateTime created;

}

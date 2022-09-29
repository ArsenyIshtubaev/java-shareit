package ru.practicum.shareit.requests.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.requests.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findAllByRequestor_IdOrderByCreatedDesc(long userId);

    List<ItemRequest> findAll();
}

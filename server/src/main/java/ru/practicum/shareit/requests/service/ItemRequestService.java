package ru.practicum.shareit.requests.service;

import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestDtoWithItems;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto save(ItemRequestDto itemRequestDto, long userId);

    List<ItemRequestDtoWithItems> findAll(long userId);

    ItemRequestDtoWithItems findById(long userId, long itemRequestId);

    List<ItemRequestDtoWithItems> findAllWithPageable(long userId, int from, int size);

    void deleteById(long itemRequestId);
}

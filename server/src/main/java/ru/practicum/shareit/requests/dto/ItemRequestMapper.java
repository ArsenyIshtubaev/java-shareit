package ru.practicum.shareit.requests.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.requests.model.ItemRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemRequestMapper {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Autowired
    public ItemRequestMapper(ItemRepository itemRepository, ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
    }

    public ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return new ItemRequestDto(itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated());
    }

    public ItemRequest toItemRequest(ItemRequestDto itemRequestDto) {
        return new ItemRequest(itemRequestDto.getId(),
                itemRequestDto.getDescription(),
                null,
                LocalDateTime.now());
    }

    public ItemRequestDtoWithItems toItemRequestDtoWithItems(ItemRequest itemRequest) {
        ItemRequestDtoWithItems itemRequestDtoWithItems = new ItemRequestDtoWithItems(itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated(),
                new ArrayList<>()
        );
        List<ItemDto> items = itemRepository.findAllByItemRequest_Id(itemRequestDtoWithItems.getId())
                .stream().map(itemMapper::toItemDto).collect(Collectors.toList());
        if (!items.isEmpty()) {
            itemRequestDtoWithItems.setItems(items);
        }
        return itemRequestDtoWithItems;

    }

}

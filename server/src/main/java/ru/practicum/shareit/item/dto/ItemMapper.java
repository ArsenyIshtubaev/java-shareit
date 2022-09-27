package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.requests.model.ItemRequest;

import java.util.ArrayList;

@Component
public class ItemMapper {

    public ItemDto toItemDto(Item item) {
        ItemDto itemDto = new ItemDto(item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                null
        );
        ItemRequest request = item.getItemRequest();
        if (request != null) {
            itemDto.setRequestId(request.getId());
        }
        return itemDto;
    }

    public Item toItem(ItemDto itemDto) {
        return new Item(itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                null,
                null);
    }

    public ItemDtoWithBooking toItemDtoWithBooking(Item item) {
        return new ItemDtoWithBooking(item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                null,
                null,
                new ArrayList<>()
        );
    }

}

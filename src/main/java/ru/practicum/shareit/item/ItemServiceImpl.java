package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.StorageException;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService{

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
    }


    @Override
    public ItemDto findById(long itemId) {
        if (itemRepository.findById(itemId).isPresent()) {
            return itemMapper.toItemDto(itemRepository.findById(itemId).get());
        }
        throw new StorageException("Вещи с Id = " + itemId + " нет в БД");
    }

    @Override
    public List<ItemDto> findAll() {
        return itemRepository.findAll().stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto save(ItemDto itemDto) {
        return itemMapper.toItemDto(itemRepository.save(itemMapper.toItem(itemDto)));
    }

    @Override
    public ItemDto update(ItemDto itemDto) {
        findById(itemDto.getId());
        return itemMapper.toItemDto(itemRepository.update(itemMapper.toItem(itemDto)));
    }

    @Override
    public void deleteById(long itemId) {
        itemRepository.deleteById(itemId);
    }
}

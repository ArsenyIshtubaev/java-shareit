package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

/**
 * // TODO .
 */
@RestController
@Slf4j
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<ItemDto> findAll() {
        return itemService.findAll();
    }

    @PostMapping
    public ItemDto create(@Valid @RequestBody ItemDto itemDto) {
        log.info("Получен запрос к эндпоинту: '{} {}', Вещь: Наименование: {} и Описание: {}", "POST", "/items",
                itemDto.getName(), itemDto.getDescription());
        return itemService.save(itemDto);
    }
    @PutMapping
    public ItemDto update(@Valid @RequestBody ItemDto itemDto) {
        log.info("Получен запрос к эндпоинту: '{} {}', Вещь: Наименование: {} и Описание: {}", "PUT", "/items",
                itemDto.getName(), itemDto.getDescription());
        return itemService.update(itemDto);
    }

    @GetMapping("/{id}")
    public ItemDto findUserById(@PathVariable long id) {
        log.info("Get item id={}", id);
        return itemService.findById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable long id){
        log.info("Delete item id={}", id);
        itemService.deleteById(id);
    }
}

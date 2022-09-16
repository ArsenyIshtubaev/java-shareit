package ru.practicum.shareit.item.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * // TODO .
 */

@RestController
@Slf4j
@Validated
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<ItemDtoWithBooking> findAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                            @RequestParam(defaultValue = "0") @Min(0) int from,
                                            @RequestParam(defaultValue = "20") @Positive int size) {
        return itemService.findAll(userId, from, size);
    }

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") long userId, @Valid @RequestBody ItemDto itemDto) {
        log.info("Получен запрос к эндпоинту: '{} {}', Вещь: Наименование: {} и Описание: {}", "POST", "/items",
                itemDto.getName(), itemDto.getDescription());
        return itemService.save(itemDto, userId);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") long userId,
                          @PathVariable long id,
                          @RequestBody ItemDto itemDto) {
        log.info("PATCH user id={}, item id={}", userId, id);
        return itemService.update(itemDto, userId, id);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @Valid @RequestBody CommentDto commentDto,
                                    @PathVariable long itemId) {
        log.info("Получен запрос к эндпоинту: '{} {}', Вещь с Id: {}, Комментарий: {}",
                "POST", "/items/{itemId}/comment",
                itemId, commentDto.getText());
        return itemService.saveComment(userId, itemId, commentDto);
    }

    @GetMapping("/{itemId}")
    public ItemDtoWithBooking findItemById(@PathVariable long itemId, @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Get item id={}", itemId);
        return itemService.findById(itemId, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteItemById(@PathVariable long id) {
        log.info("Delete item id={}", id);
        itemService.deleteById(id);
    }

    @GetMapping("/search")
    public List<ItemDto> findItemByText(@RequestParam String text,
                                        @RequestParam(defaultValue = "0") @Min(0) int from,
                                        @RequestParam(defaultValue = "20") @Positive int size) {
        log.info("Get search item text={}", text);
        return itemService.searchItem(text, from, size);
    }
}

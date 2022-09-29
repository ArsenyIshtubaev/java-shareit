package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;

/**
 * // TODO .
 */

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/items")
public class ItemController {

    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> findAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @RequestParam(defaultValue = "0") @Min(0) int from,
                                          @RequestParam(defaultValue = "20") @Positive int size) {
        return itemClient.getAll(userId, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @Valid @RequestBody ItemDto itemDto) {
        log.info("Получен запрос к эндпоинту: '{} {}', Вещь: Наименование: {} и Описание: {}", "POST", "/items",
                itemDto.getName(), itemDto.getDescription());
        return itemClient.save(userId, itemDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @PathVariable long id,
                                         @RequestBody ItemDto itemDto) {
        log.info("PATCH user id={}, item id={}", userId, id);
        return itemClient.update(userId, id, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @Valid @RequestBody CommentDto commentDto,
                                                @PathVariable long itemId) {
        log.info("Получен запрос к эндпоинту: '{} {}', Вещь с Id: {}, Комментарий: {}",
                "POST", "/items/{itemId}/comment",
                itemId, commentDto.getText());
        return itemClient.saveComment(userId, itemId, commentDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findItemById(@PathVariable long itemId,
                                               @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Get item id={}", itemId);
        return itemClient.getItem(itemId, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteItemById(@PathVariable long id) {
        log.info("Delete item id={}", id);
        itemClient.deleteById(id);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findItemByText(@RequestParam String text,
                                                 @RequestParam(defaultValue = "0") @Min(0) int from,
                                                 @RequestParam(defaultValue = "20") @Positive int size) {
        log.info("Get search item text={}", text);
        return itemClient.searchItem(text, from, size);
    }
}

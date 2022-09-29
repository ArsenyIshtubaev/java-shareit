package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.requests.dto.ItemRequestDto;

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
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader("X-Sharer-User-Id") long userId,
                                      @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Получен запрос к эндпоинту: '{} {}', Запрос на вещь: Описание: {}",
                "POST", "/requests",
                itemRequestDto.getDescription());
        return itemRequestClient.save(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> findAll(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestClient.getItemRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findByRequestId(@RequestHeader("X-Sharer-User-Id") long userId,
                                                  @RequestParam(defaultValue = "0") @Min(0) int from,
                                                  @RequestParam(defaultValue = "20") @Positive int size) {
        return itemRequestClient.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findByRequestId(@RequestHeader("X-Sharer-User-Id") long userId,
                                                  @PathVariable long requestId) {
        return itemRequestClient.getItemRequest(userId, requestId);
    }
}

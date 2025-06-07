package webgroup.websocket.controllers;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import webgroup.websocket.dto.EventDTO;
import webgroup.websocket.entities.Event;
import webgroup.websocket.mappers.EventMapper;
import webgroup.websocket.services.EventService;
import webgroup.websocket.services.NotificationService;

import java.util.List;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;
    private final NotificationService notificationService;
    private final EventMapper eventMapper;

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody EventDTO eventDto) {
        log.info("Получен запрос на создание события: {}", eventDto.getMessage());

        EventDTO savedDto = eventService.save(eventDto);

        if (savedDto.getId() == null) {
            log.error("Сохранённое событие не имеет ID! Нельзя обработать.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        log.info("Событие успешно сохранено: ID={}, message={}", savedDto.getId(), savedDto.getMessage());

        try {
            Event savedEntity = eventMapper.toEntity(savedDto); // преобразуем DTO в Entity
            notificationService.processEvent(savedEntity);      // передаём сущность в сервис
        } catch (Exception ex) {
            log.error("Ошибка при обработке события в NotificationService: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(savedDto);
        }

        return ResponseEntity.ok(savedDto);
    }


    @GetMapping
    public List<EventDTO> getAllEvents() {
        return eventService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEvent(@PathVariable Long id) {
        return eventService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EventDTO>> getEventsByUser(@PathVariable Long userId) {
        List<EventDTO> events = eventService.findByUserId(userId);
        return ResponseEntity.ok(events);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}




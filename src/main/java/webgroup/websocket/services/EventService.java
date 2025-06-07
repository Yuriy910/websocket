package webgroup.websocket.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import webgroup.websocket.dto.EventDTO;
import webgroup.websocket.entities.Event;
import webgroup.websocket.mappers.EventMapper;
import webgroup.websocket.repositories.EventRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Transactional
    public EventDTO save(EventDTO eventDto) {
        if (eventDto == null) {
            log.error("Попытка сохранить null-событие");
            throw new IllegalArgumentException("Событие не может быть null");
        }

        log.info("Попытка сохранить событие: message={}", eventDto.getMessage());

        Event event = eventMapper.toEntity(eventDto);
        Event saved = eventRepository.save(event);
        eventRepository.flush();

        log.info("Событие сохранено в БД: ID={}, message={}", saved.getId(), saved.getMessage());

        return eventMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<EventDTO> findAll() {
        log.info("Запрос на получение всех событий");
        return eventRepository.findAll().stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<EventDTO> findById(Long id) {
        if (id == null) {
            log.warn("Попытка поиска события с null ID");
            return Optional.empty();
        }

        log.info("Поиск события по ID={}", id);
        return eventRepository.findById(id).map(eventMapper::toDto);
    }

    @Transactional
    public void deleteById(Long id) {
        if (id == null) {
            log.error("Попытка удалить событие с null ID");
            throw new IllegalArgumentException("ID события не может быть null");
        }

        log.info("🗑 Запрос на удаление события ID={}", id);

        if (!eventRepository.existsById(id)) {
            log.error("Событие не найдено для удаления: ID={}", id);
            throw new RuntimeException("Событие не найдено для удаления: " + id);
        }

        eventRepository.deleteById(id);
        log.info("Событие удалено: ID={}", id);
    }

    @Transactional(readOnly = true)
    public List<EventDTO> findByUserId(Long userId) {
        if (userId == null) {
            log.warn("Попытка получить события с null userId");
            return List.of();
        }

        log.info("Поиск событий для пользователя ID={}", userId);

        return eventRepository.findByUsers_Id(userId).stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }
}





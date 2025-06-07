package webgroup.websocket.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import webgroup.websocket.dto.EventDTO;
import webgroup.websocket.entities.Event;
import webgroup.websocket.mappers.EventMapper;
import webgroup.websocket.repositories.EventRepository;

public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventService eventService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_shouldSaveEvent() {
        EventDTO dto = new EventDTO();
        dto.setMessage("Test event");

        Event event = new Event();
        event.setMessage("Test event");

        Event savedEvent = new Event();
        savedEvent.setId(1L);
        savedEvent.setMessage("Test event");

        EventDTO savedDto = new EventDTO();
        savedDto.setId(1L);
        savedDto.setMessage("Test event");

        when(eventMapper.toEntity(dto)).thenReturn(event);
        when(eventRepository.save(event)).thenReturn(savedEvent);
        doNothing().when(eventRepository).flush();
        when(eventMapper.toDto(savedEvent)).thenReturn(savedDto);

        EventDTO result = eventService.save(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test event", result.getMessage());

        verify(eventMapper).toEntity(dto);
        verify(eventRepository).save(event);
        verify(eventRepository).flush();
        verify(eventMapper).toDto(savedEvent);
    }

    @Test
    void save_shouldThrowException_whenDtoIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            eventService.save(null);
        });

        assertEquals("Событие не может быть null", ex.getMessage());
    }

    @Test
    void findAll_shouldReturnMappedList() {
        Event event1 = new Event();
        event1.setId(1L);
        Event event2 = new Event();
        event2.setId(2L);

        EventDTO dto1 = new EventDTO();
        dto1.setId(1L);
        EventDTO dto2 = new EventDTO();
        dto2.setId(2L);

        when(eventRepository.findAll()).thenReturn(List.of(event1, event2));
        when(eventMapper.toDto(event1)).thenReturn(dto1);
        when(eventMapper.toDto(event2)).thenReturn(dto2);

        List<EventDTO> result = eventService.findAll();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void findById_shouldReturnDto_whenFound() {
        Event event = new Event();
        event.setId(1L);

        EventDTO dto = new EventDTO();
        dto.setId(1L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventMapper.toDto(event)).thenReturn(dto);

        Optional<EventDTO> result = eventService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void findById_shouldReturnEmpty_whenIdIsNull() {
        Optional<EventDTO> result = eventService.findById(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteById_shouldDelete_whenExists() {
        Long id = 1L;
        when(eventRepository.existsById(id)).thenReturn(true);
        doNothing().when(eventRepository).deleteById(id);

        assertDoesNotThrow(() -> eventService.deleteById(id));

        verify(eventRepository).existsById(id);
        verify(eventRepository).deleteById(id);
    }

    @Test
    void deleteById_shouldThrowException_whenNotExists() {
        Long id = 1L;
        when(eventRepository.existsById(id)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> eventService.deleteById(id));
        assertEquals("Событие не найдено для удаления: " + id, ex.getMessage());

        verify(eventRepository).existsById(id);
        verify(eventRepository, never()).deleteById(anyLong());
    }

    @Test
    void findByUserId_shouldReturnEvents() {
        Long userId = 1L;

        Event event = new Event();
        event.setId(10L);
        EventDTO dto = new EventDTO();
        dto.setId(10L);

        when(eventRepository.findByUsers_Id(userId)).thenReturn(List.of(event));
        when(eventMapper.toDto(event)).thenReturn(dto);

        List<EventDTO> result = eventService.findByUserId(userId);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    void findByUserId_shouldReturnEmpty_whenUserIdNull() {
        List<EventDTO> result = eventService.findByUserId(null);
        assertTrue(result.isEmpty());
    }
}
